package com.arubr.smsvcodes.ui.screens.home

import com.arubr.smsvcodes.data.local.VideoHistoryEntry
import com.arubr.smsvcodes.data.model.Video

data class HomeUiState(
    val videos: List<Video> = emptyList(),
    val shorts: List<Video> = emptyList(),
    val continueWatchingVideos: List<VideoHistoryEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMorePages: Boolean = true,
    val error: String? = null,
    val isFlowFeed: Boolean = false,
    val lastRefreshTime: Long = 0L,
    /** Videos the channel memory lane brought, which say so under their title. */
    val channelMemoryVideoIds: Set<String> = emptySet(),
)
