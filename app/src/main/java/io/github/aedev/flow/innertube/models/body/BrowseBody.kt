package com.arubr.smsvcodes.innertube.models.body

import com.arubr.smsvcodes.innertube.models.Context
import com.arubr.smsvcodes.innertube.models.Continuation
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?,
    val query: String? = null,
)
