package com.arubr.smsvcodes.ui.components.videoplayer.overlay

import com.arubr.smsvcodes.data.local.SponsorBlockAction
import com.arubr.smsvcodes.data.model.SponsorBlockCategories
import com.arubr.smsvcodes.data.model.SponsorBlockSegment

internal fun findActiveManualSponsorSegment(
    sponsorSegments: List<SponsorBlockSegment>,
    currentPositionMs: Long,
    skippedUuids: Set<String>,
    categoryActions: Map<String, SponsorBlockAction>,
    playbackEnded: Boolean,
): SponsorBlockSegment? {
    if (playbackEnded) return null

    val positionSeconds = currentPositionMs / 1000f
    return sponsorSegments.find { segment ->
        positionSeconds >= segment.startTime &&
            positionSeconds < segment.endTime &&
            segment.uuid !in skippedUuids &&
            (categoryActions[segment.category] ?: SponsorBlockCategories.defaultAction(segment.category)) !=
            SponsorBlockAction.SKIP
    }
}
