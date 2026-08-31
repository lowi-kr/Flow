package com.arubr.smsvcodes.innertube.models.body

import com.arubr.smsvcodes.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
