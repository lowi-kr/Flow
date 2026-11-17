package com.arubr.smsvcodes.ui.components.shared.card

import com.arubr.smsvcodes.data.localmedia.LocalMediaIds
import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.ui.components.DeArrowLookup

/** What a card's thumbnail loads: [url] null holds the empty container, [youTubeFrames] as in [VideoCardState]. */
internal data class CardThumbnail(
    val url: String?,
    val youTubeFrames: Boolean,
)

/**
 * The card's image under DeArrow. With [thumbnailsOnly] the uploader's thumbnail is never requested
 * (#1633): the card waits for DeArrow's answer, then shows DeArrow's frame or YouTube's own. Shorts,
 * live and upcoming videos and device files keep their own image, since YouTube has no frame for them.
 */
internal object CardThumbnailPolicy {
    fun resolve(
        video: Video,
        lookup: DeArrowLookup,
        thumbnailsOnly: Boolean,
    ): CardThumbnail {
        val framesOnly = thumbnailsOnly && keepsNoOwnImage(video)
        if (framesOnly && !lookup.answered) return CardThumbnail(url = null, youTubeFrames = true)
        return CardThumbnail(
            url = lookup.result?.thumbnailUrl ?: video.thumbnailUrl,
            youTubeFrames = framesOnly || lookup.result?.youTubeFrames == true,
        )
    }

    private fun keepsNoOwnImage(video: Video): Boolean =
        !video.isShort && !video.isLive && !video.isUpcoming && !LocalMediaIds.isLocal(video.id)
}
