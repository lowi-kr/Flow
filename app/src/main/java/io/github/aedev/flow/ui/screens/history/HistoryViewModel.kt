package com.arubr.smsvcodes.ui.screens.history

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arubr.smsvcodes.data.local.AppDatabase
import com.arubr.smsvcodes.data.local.ViewHistory
import com.arubr.smsvcodes.data.local.VideoHistoryEntry
import com.arubr.smsvcodes.data.local.entity.WatchHistoryEntity
import com.arubr.smsvcodes.data.local.entity.VideoEntity
import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.data.repository.YouTubeRepository
import com.arubr.smsvcodes.utils.ThumbnailUrlResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class HistoryViewModel : ViewModel() {

    private lateinit var viewHistory: ViewHistory
    private val youTubeRepository = YouTubeRepository.getInstance()
    private val isEnriching = AtomicBoolean(false)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun initialize(context: Context) {
        viewHistory = ViewHistory.getInstance(context)
        val videoDao = AppDatabase.getDatabase(context).videoDao()
        val watchHistoryDao = AppDatabase.getDatabase(context).watchHistoryDao()

        // Load history and enrich any entries that are missing metadata
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            viewHistory.getAllHistory().collect { history ->
                val enriched = history.map { entry ->
                    var e = entry

                    val needsEnrichment = e.title.isEmpty() || e.channelName.isEmpty()
                    val dbVideo = if (needsEnrichment || e.isShort) videoDao.getVideo(e.videoId) else null

                    if (e.thumbnailUrl.isEmpty()) {
                        e = e.copy(
                            thumbnailUrl = ThumbnailUrlResolver.normalizeVideoThumbnail(
                                e.videoId,
                                dbVideo?.thumbnailUrl
                            )
                        )
                    }

                    if (dbVideo != null) {
                        if (e.title.isEmpty() && dbVideo.title.isNotEmpty())
                            e = e.copy(title = dbVideo.title)
                        if (e.channelName.isEmpty() && dbVideo.channelName.isNotEmpty())
                            e = e.copy(channelName = dbVideo.channelName, channelId = dbVideo.channelId)
                        if (dbVideo.thumbnailUrl.isNotEmpty() &&
                            ThumbnailUrlResolver.isYoutubeVideoThumbnail(e.thumbnailUrl))
                            e = e.copy(thumbnailUrl = dbVideo.thumbnailUrl)
                    }
                    e
                }

                val shortVideos = mutableMapOf<String, Video>()
                enriched
                    .filter { it.isShort }
                    .forEach { entry ->
                        val video = videoDao.getVideo(entry.videoId)?.toDomain()?.copy(
                            isShort = true,
                            isMusic = entry.isMusic,
                            timestamp = entry.timestamp
                        )
                        if (video != null) {
                            shortVideos[video.id] = video
                        }
                    }

                _uiState.update {
                    it.copy(
                        historyEntries = enriched,
                        shortVideos = shortVideos,
                        isLoading = false
                    )
                }

                val stubs = enriched
                    .filter { entry ->
                        !entry.isLocal && (
                            entry.title.isEmpty() ||
                                entry.channelName.isEmpty() ||
                                (entry.isShort && !shortVideos.containsKey(entry.videoId))
                        )
                    }
                    .distinctBy { it.videoId }
                    .take(30)
                if (stubs.isNotEmpty()) {
                    enrichFromApi(stubs, videoDao, watchHistoryDao)
                }
            }
        }
    }

    private fun enrichFromApi(
        stubs: List<VideoHistoryEntry>,
        videoDao: com.arubr.smsvcodes.data.local.dao.VideoDao,
        watchHistoryDao: com.arubr.smsvcodes.data.local.dao.WatchHistoryDao
    ) {
        if (!isEnriching.compareAndSet(false, true)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stubs.chunked(5).forEach { chunk ->
                    chunk.forEach { stub ->
                        try {
                            val video = youTubeRepository.getVideo(stub.videoId) ?: return@forEach
                            val e = VideoEntity.fromDomain(video)
                            videoDao.insertVideoOrIgnore(e) 
                            videoDao.updateVideoMetadata(
                                id = e.id,
                                title = e.title,
                                channelName = e.channelName,
                                channelId = e.channelId,
                                thumbnailUrl = e.thumbnailUrl,
                                duration = e.duration,
                                viewCount = e.viewCount,
                                uploadDate = e.uploadDate,
                                description = e.description,
                                channelThumbnailUrl = e.channelThumbnailUrl
                            )
                            watchHistoryDao.upsert(
                                WatchHistoryEntity(
                                    videoId      = stub.videoId,
                                    position     = stub.position,
                                    duration     = video.duration * 1000L,
                                    timestamp    = stub.timestamp,
                                    title        = video.title,
                                    thumbnailUrl = ThumbnailUrlResolver.normalizeVideoThumbnail(
                                        stub.videoId,
                                        video.thumbnailUrl
                                    ),
                                    channelName  = video.channelName,
                                    channelId    = video.channelId,
                                    isMusic      = stub.isMusic,
                                    isShort      = stub.isShort || video.isShort
                                )
                            )
                        } catch (_: Exception) { /* skip individual failures */ }
                    }
                    delay(300L)
                }
            } finally {
                isEnriching.set(false)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            viewHistory.clearAllHistory()
        }
    }

    fun clearShortsHistory() {
        viewModelScope.launch {
            viewHistory.clearShortsHistory()
        }
    }

    fun removeFromHistory(videoId: String) {
        viewModelScope.launch {
            viewHistory.clearVideoHistory(videoId)
        }
    }
}

data class HistoryUiState(
    val historyEntries: List<VideoHistoryEntry> = emptyList(),
    val shortVideos: Map<String, Video> = emptyMap(),
    val isLoading: Boolean = false
)

