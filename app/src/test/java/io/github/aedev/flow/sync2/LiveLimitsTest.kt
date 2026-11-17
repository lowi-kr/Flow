package com.arubr.smsvcodes.sync2

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arubr.smsvcodes.sync.crypto.SyncBytes
import com.arubr.smsvcodes.sync.crypto.SyncCrypto
import com.arubr.smsvcodes.sync2.crypto.Purpose
import com.arubr.smsvcodes.sync2.crypto.encodePayload
import com.arubr.smsvcodes.sync2.crypto.purposeKey
import com.arubr.smsvcodes.sync2.engine.Session
import com.arubr.smsvcodes.sync2.live.LiveMessage
import com.arubr.smsvcodes.sync2.live.openLive
import com.arubr.smsvcodes.sync2.live.sealLive
import com.arubr.smsvcodes.sync2.store.Header
import com.arubr.smsvcodes.sync2.store.MAX_LIVE_MESSAGE
import com.arubr.smsvcodes.sync2.store.ObjectType
import com.arubr.smsvcodes.sync2.store.sealObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A live message is at most 64 KiB and has no `prev` (spec/store.md), on every transport. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class)
class LiveLimitsTest {
    @Test
    fun a_live_message_over_the_limit_is_dropped() =
        runTest {
            val g = LinkedGroup(this).apply { create() }
            val fromA = Session.open(g.deviceDb(), g.store, g.a)
            val toB = Session.open(g.deviceDb(), g.store, g.b)
            assertNotNull(toB.openLive(fromA.sealLive(LiveMessage.Now("v", title = "t"), 1)))

            val title = SyncBytes.toHex(SyncCrypto.randomBytes(100 * 1024))
            val big = fromA.sealLive(LiveMessage.Now("v", title = title), 2)

            assertTrue(big.size > MAX_LIVE_MESSAGE)
            assertNull(toB.openLive(big))
        }

    @Test
    fun a_live_message_naming_a_previous_object_is_dropped() =
        runTest {
            val g = LinkedGroup(this).apply { create() }
            val toB = Session.open(g.deviceDb(), g.store, g.b)
            val key = purposeKey(g.epochKey, g.group, 0, Purpose.LIVE)
            val content = encodePayload(LiveMessage.encode(LiveMessage.Now("v")))

            fun envelope(prev: ByteArray) = sealObject(Header(ObjectType.LIVE, g.group, 0, g.a.did, 1, prev), key, content, g.a.sigSeed)

            assertEquals(1L, toB.openLive(envelope(ByteArray(32)))?.seq)
            assertNull(toB.openLive(envelope(ByteArray(32) { 1 })))
        }
}
