package com.arubr.smsvcodes.service.auto

import com.arubr.smsvcodes.data.local.LikedVideosRepository
import com.arubr.smsvcodes.data.local.PlaylistRepository
import com.arubr.smsvcodes.data.model.PlaylistInfo
import com.arubr.smsvcodes.data.model.toMusicTrack
import com.arubr.smsvcodes.data.music.DownloadManager
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.data.music.model.audioMusicOnly
import com.arubr.smsvcodes.data.music.model.toMusicTrack
import com.arubr.smsvcodes.data.newmusic.InnertubeMusicService
import com.arubr.smsvcodes.data.recommendation.music.MusicBrainEngine
import com.arubr.smsvcodes.data.recommendation.music.onRepeatShelf
import com.arubr.smsvcodes.data.music.zemer.ZemerFilter
import com.arubr.smsvcodes.player.EnhancedMusicPlayerManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import com.arubr.smsvcodes.data.music.PlaylistRepository as MusicHistoryRepository

/**
 * What the car lists, read from what the app already stores. Browsing makes no network call; only a
 * search does, once per query within [SEARCH_TTL_MS].
 */
@Singleton
class AutoLibrary
    @Inject
    constructor(
        private val musicBrain: MusicBrainEngine,
        private val liked: LikedVideosRepository,
        private val downloads: DownloadManager,
        private val musicHistory: MusicHistoryRepository,
        private val playlists: PlaylistRepository,
    ) {
        private data class CachedSearch(
            val at: Long,
            val tracks: List<MusicTrack>,
        )

        private val searchLock = Mutex()
        private val searches = LinkedHashMap<String, CachedSearch>()

        suspend fun playlists(): List<PlaylistInfo> = playlists.getMusicPlaylistsFlow().first()

        /**
         * The songs under [parentId], or null when it is not a list of songs. When the Zemer
         * filter is on, this blocks every source here - including liked songs, downloads, and
         * recent history - not just catalog browsing, per the hard "nothing outside the
         * whitelist plays" rule; [com.arubr.smsvcodes.player.EnhancedMusicPlayerManager] and
         * [AutoLibraryBrowser] enforce the same rule again at the actual playback step, so this
         * is belt-and-suspenders for the car's browse UI, not the only gate.
         */
        suspend fun tracks(parentId: String): List<MusicTrack>? {
            AutoBrowseTree.playlistIdOf(parentId)?.let { id ->
                return ZemerFilter.filterTracks(playlists.getPlaylistVideosFlow(id).first().map { it.toMusicTrack() })
            }
            AutoBrowseTree.searchQueryOf(parentId)?.let { return search(it) }
            val result =
                when (parentId) {
                    AutoBrowseTree.ON_REPEAT -> musicBrain.onRepeatShelf()
                    AutoBrowseTree.RIGHT_NOW -> musicBrain.timeOfDayTracks(MIX_SIZE).audioMusicOnly()
                    AutoBrowseTree.REDISCOVER -> musicBrain.rediscoverTracks(MIX_SIZE).audioMusicOnly()
                    AutoBrowseTree.LIKED -> liked.getLikedMusicFlow().first().map { it.toMusicTrack() }
                    AutoBrowseTree.DOWNLOADS -> downloads.downloadedTracks.first().map { it.track }
                    AutoBrowseTree.RECENT -> musicHistory.history.first()
                    AutoBrowseTree.QUEUE -> EnhancedMusicPlayerManager.queue.value
                    else -> null
                }
            return result?.let { ZemerFilter.filterTracks(it) }
        }

        /** Songs for [query], from the cache when the same words were searched a moment ago. */
        suspend fun search(query: String): List<MusicTrack> {
            val key = query.trim().lowercase()
            if (key.isEmpty()) return emptyList()
            return searchLock.withLock {
                val now = System.currentTimeMillis()
                searches[key]?.takeIf { now - it.at < SEARCH_TTL_MS }?.let { return@withLock it.tracks }
                val tracks = ZemerFilter.filterTracks(InnertubeMusicService.searchMusic(query.trim()))
                searches[key] = CachedSearch(now, tracks)
                while (searches.size > MAX_CACHED_SEARCHES) searches.remove(searches.keys.first())
                tracks
            }
        }

        private companion object {
            const val MIX_SIZE = 30
            const val SEARCH_TTL_MS = 10 * 60 * 1000L
            const val MAX_CACHED_SEARCHES = 8
        }
    }
