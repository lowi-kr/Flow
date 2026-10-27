package com.arubr.smsvcodes.innertube.models.response

import com.arubr.smsvcodes.innertube.models.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class ResolveUrlResponse(
    val endpoint: NavigationEndpoint? = null,
)
