package com.arubr.smsvcodes.innertube.pages

import com.arubr.smsvcodes.innertube.models.AlbumItem

data class ExplorePage(
    val newReleaseAlbums: List<AlbumItem>,
    val moodAndGenres: List<MoodAndGenres.Item>,
)
