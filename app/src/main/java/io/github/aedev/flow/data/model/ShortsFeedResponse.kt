package com.arubr.smsvcodes.data.model

data class ShortsFeedResponse(
    val videos: List<ShortItem>,
    val nextContinuationToken: String?
)
