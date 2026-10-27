package com.arubr.smsvcodes.player

import com.arubr.smsvcodes.data.recommendation.FeedExclusions

/** What the viewer hid, so autoplay picked outside the player screen skips it too. */
fun interface FeedExclusionsSource {
    suspend fun current(): FeedExclusions
}
