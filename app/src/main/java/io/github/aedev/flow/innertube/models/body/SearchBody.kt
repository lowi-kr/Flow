package com.arubr.smsvcodes.innertube.models.body

import com.arubr.smsvcodes.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String?,
    val params: String?,
)
