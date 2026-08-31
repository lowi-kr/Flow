package com.arubr.smsvcodes.ui.screens.library

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.data.music.DownloadedTrack
import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.data.video.DownloadedVideo
import com.arubr.smsvcodes.ui.components.PlaylistCard
import com.arubr.smsvcodes.ui.components.PlaylistCardLayout
import com.arubr.smsvcodes.ui.screens.music.MusicTrack
import com.arubr.smsvcodes.ui.screens.playlists.PlaylistInfo
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun LibraryMediaShelfRoute(
    title: String,
    itemsFlow: StateFlow<List<LibraryMediaItem>>,
    sourceName: String,
    onTitleClick: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onMusicClick: (MusicTrack, List<MusicTrack>, String) -> Unit,
    onDownloadedVideoClick: (List<DownloadedVideo>, Int) -> Unit,
    onDownloadedMusicClick: (List<DownloadedTrack>, Int) -> Unit
) {
    val items by itemsFlow.collectAsStateWithLifecycle()
    LibraryMediaShelf(
        title = title,
        items = items,
        sourceName = sourceName,
        onTitleClick = onTitleClick,
        onVideoClick = onVideoClick,
        onMusicClick = onMusicClick,
        onDownloadedVideoClick = onDownloadedVideoClick,
        onDownloadedMusicClick = onDownloadedMusicClick
    )
}

@Composable
internal fun LibraryPlaylistsShelf(
    title: String,
    videoPlaylistsFlow: StateFlow<List<PlaylistInfo>>,
    musicPlaylistsFlow: StateFlow<List<PlaylistInfo>>,
    onTitleClick: () -> Unit,
    onVideoPlaylistClick: (String) -> Unit,
    onMusicPlaylistClick: (String) -> Unit
) {
    val videoPlaylists by videoPlaylistsFlow.collectAsStateWithLifecycle()
    val musicPlaylists by musicPlaylistsFlow.collectAsStateWithLifecycle()
    LibraryShelf(title = title, onTitleClick = onTitleClick) {
        items(
            items = videoPlaylists,
            key = { "video-${it.id}" },
            contentType = { "video-playlist" }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onVideoPlaylistClick(playlist.id) },
                layout = PlaylistCardLayout.SHELF,
                modifier = Modifier.width(LibraryShelfCardWidth)
            )
        }
        items(
            items = musicPlaylists,
            key = { "music-${it.id}" },
            contentType = { "music-playlist" }
        ) { playlist ->
            LibraryAlbumCard(
                title = playlist.name,
                subtitle = stringResource(R.string.tracks_count_template, playlist.videoCount),
                thumbnailUrl = playlist.thumbnailUrl,
                onClick = { onMusicPlaylistClick(playlist.id) }
            )
        }
    }
}

@Composable
internal fun LibraryVideoShelf(
    title: String,
    videosFlow: StateFlow<List<Video>>,
    onTitleClick: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val videos by videosFlow.collectAsStateWithLifecycle()
    LibraryShelf(title = title, onTitleClick = onTitleClick) {
        items(
            items = videos,
            key = Video::id,
            contentType = { "video" }
        ) { video ->
            LibraryVideoCard(
                video = video,
                onClick = { onVideoClick(video) }
            )
        }
    }
}

@Composable
internal fun LibraryShortsShelfRoute(
    title: String,
    shortsFlow: StateFlow<List<Video>>,
    onTitleClick: () -> Unit,
    onShortClick: (Video) -> Unit
) {
    val shorts by shortsFlow.collectAsStateWithLifecycle()
    LibraryShortsShelf(
        title = title,
        shorts = shorts,
        onTitleClick = onTitleClick,
        onShortClick = onShortClick
    )
}
