package com.arubr.smsvcodes.sync2.engine

import com.arubr.smsvcodes.sync2.data.LinkedDatabase
import com.arubr.smsvcodes.sync2.data.toEntries
import com.arubr.smsvcodes.sync2.roster.Role
import com.arubr.smsvcodes.sync2.roster.replayRoster
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val vv = MapSerializer(String.serializer(), Long.serializer())

/**
 * The current devices that haven't acked the login [me] wrote, or null when it has no change under
 * way (spec/protocol.md, Replacing a store's login).
 */
internal suspend fun loginWaiting(
    db: LinkedDatabase,
    me: String,
): List<String>? {
    val sent = db.groups().group()?.loginSent ?: return null
    val acks = db.collection().acks().associate { it.did to Json.decodeFromString(vv, it.vv) }
    return replayRoster(db.groups().roster().toEntries())
        .members
        .filter { (did, member) -> member.role == Role.DEVICE && did != me && (acks[did]?.get(me) ?: 0) < sent }
        .keys
        .toList()
}
