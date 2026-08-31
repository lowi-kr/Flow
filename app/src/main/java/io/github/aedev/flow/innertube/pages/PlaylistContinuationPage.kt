package com.arubr.smsvcodes.innertube.pages

import com.arubr.smsvcodes.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
