package com.arubr.smsvcodes.innertube.pages

import com.arubr.smsvcodes.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
