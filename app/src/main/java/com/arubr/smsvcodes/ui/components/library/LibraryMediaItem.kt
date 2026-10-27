package com.arubr.smsvcodes.ui.components.library

import com.arubr.smsvcodes.data.local.LikedVideoInfo
import com.arubr.smsvcodes.data.local.VideoHistoryEntry
import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.data.model.toMusicTrack
import com.arubr.smsvcodes.data.model.toVideo
import com.arubr.smsvcodes.data.music.DownloadedTrack
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.data.video.DownloadedVideo

internal const val LIBRARY_SHELF_ITEM_LIMIT = 20

internal sealed interface LibraryMediaItem {
    val key: String

    data class VideoItem(
        val video: Video,
    ) : LibraryMediaItem {
        override val key: String = "video:${video.id}"
    }

    data class MusicItem(
        val track: MusicTrack,
    ) : LibraryMediaItem {
        override val key: String = "music:${track.videoId}"
    }

    data class DownloadedVideoItem(
        val download: DownloadedVideo,
    ) : LibraryMediaItem {
        override val key: String = "downloaded-video:${download.video.id}"
    }

    data class DownloadedMusicItem(
        val download: DownloadedTrack,
    ) : LibraryMediaItem {
        override val key: String = "downloaded-music:${download.track.videoId}"
    }
}

internal fun VideoHistoryEntry.toLibraryMediaItem(): LibraryMediaItem =
    if (isMusic) {
        LibraryMediaItem.MusicItem(toMusicTrack())
    } else {
        LibraryMediaItem.VideoItem(toVideo())
    }

internal fun LikedVideoInfo.toLibraryMediaItem(): LibraryMediaItem =
    if (isMusic) {
        LibraryMediaItem.MusicItem(toMusicTrack())
    } else {
        LibraryMediaItem.VideoItem(toVideo())
    }
