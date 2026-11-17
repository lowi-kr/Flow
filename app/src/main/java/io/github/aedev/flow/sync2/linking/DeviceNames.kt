package com.arubr.smsvcodes.sync2.linking

import com.arubr.smsvcodes.sync.crypto.SyncBytes
import com.arubr.smsvcodes.sync2.crypto.ProtocolException
import com.arubr.smsvcodes.sync2.engine.LinkedException
import com.arubr.smsvcodes.sync2.engine.Session
import com.arubr.smsvcodes.sync2.roster.Entry
import com.arubr.smsvcodes.sync2.roster.RosterState
import com.arubr.smsvcodes.sync2.roster.applyEntry
import com.arubr.smsvcodes.sync2.roster.openDeviceMeta

/**
 * Device names and platforms, as the latest roster entry naming each device says. Names are only
 * shown, so one that can't be read is left out instead of stopping sync.
 */
suspend fun deviceNames(session: Session): Map<String, DeviceMeta> = deviceNames(session.entries, session.group, session::epochKey)

internal suspend fun deviceNames(
    entries: List<Entry>,
    group: ByteArray,
    epochKey: suspend (Int) -> ByteArray,
): Map<String, DeviceMeta> {
    val names = HashMap<String, DeviceMeta>()
    var state: RosterState? = null
    for (entry in entries) {
        val after = applyEntry(state, entry)
        for ((did, sealed) in entry.meta.orEmpty()) {
            openName(group, epochKey, after.epoch, entry.seq, did, sealed)?.let { names[did] = it }
        }
        state = after
    }
    return names
}

private suspend fun openName(
    group: ByteArray,
    epochKey: suspend (Int) -> ByteArray,
    epoch: Int,
    seq: Long,
    did: String,
    sealed: String,
): DeviceMeta? =
    try {
        val content =
            openDeviceMeta(
                epochKey(epoch),
                group,
                epoch,
                seq,
                SyncBytes.fromHex(did),
                SyncBytes.b64urlDecode(sealed),
            )
        pairingJson.decodeFromString(DeviceMeta.serializer(), content)
    } catch (_: ProtocolException) {
        null
    } catch (_: LinkedException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
