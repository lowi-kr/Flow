package com.arubr.smsvcodes.innertube.pages

import com.arubr.smsvcodes.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
