package com.arubr.smsvcodes.sync2.live

import com.arubr.smsvcodes.sync.crypto.SyncBytes
import com.arubr.smsvcodes.sync2.crypto.Purpose
import com.arubr.smsvcodes.sync2.crypto.decodePayload
import com.arubr.smsvcodes.sync2.crypto.encodePayload
import com.arubr.smsvcodes.sync2.crypto.purposeKey
import com.arubr.smsvcodes.sync2.engine.Session
import com.arubr.smsvcodes.sync2.roster.Role
import com.arubr.smsvcodes.sync2.store.Header
import com.arubr.smsvcodes.sync2.store.MAX_LIVE_MESSAGE
import com.arubr.smsvcodes.sync2.store.ObjectType
import com.arubr.smsvcodes.sync2.store.ZERO_HASH
import com.arubr.smsvcodes.sync2.store.parseObject
import com.arubr.smsvcodes.sync2.store.sealObject
import kotlinx.coroutines.CancellationException

/** A live message another member sent, with its live counter. */
class LiveReceived(
    val sender: String,
    val seq: Long,
    val message: LiveMessage,
)

/** Seals [message] as a live envelope: type `0x10` under `P_e,live`, [seq] the live counter, `prev` zero (spec/live.md). */
internal fun Session.sealLive(
    message: LiveMessage,
    seq: Long,
): ByteArray {
    val epoch = state.epoch
    val header = Header(ObjectType.LIVE, group, epoch, device.did, seq, ZERO_HASH)
    val key = purposeKey(sealingKey(), group, epoch, Purpose.LIVE)
    return sealObject(header, key, encodePayload(LiveMessage.encode(message)), device.sigSeed)
}

/** Opens a valid live message signed by another current device of this group, or null for anything else. */
internal suspend fun Session.openLive(bytes: ByteArray): LiveReceived? =
    try {
        if (bytes.size > MAX_LIVE_MESSAGE) return null
        val parsed = parseObject(bytes, group)
        val header = parsed.header
        val sender = SyncBytes.toHex(header.sender)
        val member = state.members[sender]?.takeIf { it.role == Role.DEVICE }
        if (header.type != ObjectType.LIVE || !header.prev.contentEquals(ZERO_HASH) || sender == me || member == null) {
            null
        } else {
            parsed.verify(member.sig)
            val content = decodePayload(parsed.open(purposeKey(epochKey(header.epoch), group, header.epoch, Purpose.LIVE)))
            LiveMessage.decode(content).takeIf { it.isValid() }?.let { LiveReceived(sender, header.seq, it) }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
