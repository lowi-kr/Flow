package com.arubr.smsvcodes.data.subscriptions

import com.arubr.smsvcodes.data.local.AppDatabase
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.data.local.ViewHistory
import com.arubr.smsvcodes.data.local.isWatched
import com.arubr.smsvcodes.data.shorts.finishedShortIds
import com.arubr.smsvcodes.di.DeviceDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionWatchedVideos
    @Inject
    constructor(
        private val viewHistory: ViewHistory,
        private val playerPreferences: PlayerPreferences,
        @DeviceDb private val database: AppDatabase,
    ) {
        val ids: Flow<Set<String>> =
            combine(
                viewHistory.getVideoWatchProgress(),
                playerPreferences.hideWatchedVideosFromSubscriptions,
                playerPreferences.watchedThreshold,
                playerPreferences.countImportedAsWatched,
                database.downloadDao().getVideoDownloads(),
            ) { history, hideWatched, threshold, countImported, downloads ->
                if (!hideWatched) return@combine emptySet<String>()
                val downloadedIds = downloads.mapTo(HashSet()) { it.download.videoId }
                history
                    .asSequence()
                    .filter { threshold.isWatched(it.position, it.duration, countImported) || it.videoId in downloadedIds }
                    .map { it.videoId }
                    .toHashSet()
            }.distinctUntilChanged()

        /** Every video with a history entry, finished or only started; their channels get no Quick access dot. */
        val startedIds: Flow<Set<String>> =
            viewHistory
                .getVideoWatchProgress()
                .map { history -> history.mapTo(HashSet()) { it.videoId } }
                .distinctUntilChanged()

        /** Watched reels, for the Shorts shelf and the Shorts queues; governed by the Shorts setting alone. */
        val shortIds: Flow<Set<String>> =
            combine(
                viewHistory.getVideoWatchProgress(),
                playerPreferences.hideWatchedShorts,
            ) { history, hideWatched ->
                if (hideWatched) history.finishedShortIds() else emptySet()
            }.distinctUntilChanged()
    }
