package com.arubr.smsvcodes.sync2.live

import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.data.newmusic.InnertubeMusicService
import com.arubr.smsvcodes.data.repository.YouTubeRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Titles, artists or channels, and art for what another device names only by id, from the music
 * service and the video details lookup. Each id is asked once.
 */
@Singleton
class TrackDetails
    @Inject
    constructor(
        private val youtube: YouTubeRepository,
    ) {
        private val songs = ConcurrentHashMap<String, MusicTrack>()
        private val videos = ConcurrentHashMap<String, Video>()

        fun knownSong(id: String): MusicTrack? = songs[id]

        fun knownVideo(id: String): Video? = videos[id]

        /** The songs among [ids] the music service knows, in one request for the ones not asked yet. */
        suspend fun songs(ids: List<String>): Map<String, MusicTrack> {
            val missing = ids.distinct().filterNot(songs::containsKey)
            if (missing.isNotEmpty()) InnertubeMusicService.fetchQueue(videoIds = missing).forEach { songs[it.videoId] = it }
            return ids.mapNotNull { id -> songs[id]?.let { id to it } }.toMap()
        }

        suspend fun video(id: String): Video? = videos[id] ?: youtube.getVideo(id)?.also { videos[id] = it }
    }
