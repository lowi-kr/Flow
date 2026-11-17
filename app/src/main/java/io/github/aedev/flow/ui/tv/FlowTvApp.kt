package com.arubr.smsvcodes.ui.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.player.EnhancedMusicPlayerManager
import com.arubr.smsvcodes.player.GlobalPlayerState
import com.arubr.smsvcodes.ui.components.music.common.ProvideMusicPlaybackState
import com.arubr.smsvcodes.ui.components.music.sheet.LocalMusicMenus
import com.arubr.smsvcodes.ui.components.music.sheet.MusicMenuSheets
import com.arubr.smsvcodes.ui.components.music.sheet.rememberMusicMenus
import com.arubr.smsvcodes.ui.components.settings.SettingsDestination
import com.arubr.smsvcodes.ui.components.settings.SettingsTarget
import com.arubr.smsvcodes.ui.components.shared.DpadReturnFocus
import com.arubr.smsvcodes.ui.components.shared.LocalDpadReturnFocus
import com.arubr.smsvcodes.ui.components.shared.LocalFlowSheetPresenter
import com.arubr.smsvcodes.ui.mediaNavigationLocals
import com.arubr.smsvcodes.ui.screens.home.HomeViewModel
import com.arubr.smsvcodes.ui.screens.music.MusicPlayerViewModel
import com.arubr.smsvcodes.ui.screens.music.MusicViewModel
import com.arubr.smsvcodes.ui.screens.player.VideoPlayerViewModel
import com.arubr.smsvcodes.ui.screens.playon.PlayOnHost
import com.arubr.smsvcodes.ui.screens.search.SearchViewModel
import com.arubr.smsvcodes.ui.screens.subscriptions.SubscriptionsViewModel
import com.arubr.smsvcodes.ui.tv.components.TvSheetPresenter
import com.arubr.smsvcodes.ui.tv.music.TvMusicNowPlaying
import com.arubr.smsvcodes.ui.tv.navigation.TvMediaNavigator
import com.arubr.smsvcodes.ui.tv.player.TvPlayerScreen
import com.arubr.smsvcodes.ui.tv.theme.FlowTvTheme
import kotlinx.coroutines.flow.first

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun FlowTvApp(
    deeplinkVideoId: String? = null,
    isShort: Boolean = false,
    onDeeplinkConsumed: () -> Unit = {},
    startSettings: SettingsTarget? = null,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val homeViewModel: HomeViewModel = hiltViewModel(activity)
    val playerViewModel: VideoPlayerViewModel = hiltViewModel(activity)
    val subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(activity)
    val searchViewModel: SearchViewModel = hiltViewModel(activity)
    val musicViewModel: MusicViewModel = hiltViewModel(activity)
    val musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel(activity)
    val shellViewModel: TvShellViewModel = hiltViewModel(activity)
    val navigation by shellViewModel.navigation.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val activeVideo by GlobalPlayerState.currentVideo.collectAsStateWithLifecycle()
    val activeMusicTrack by EnhancedMusicPlayerManager.currentTrack.collectAsStateWithLifecycle()
    val musicPlayerState by EnhancedMusicPlayerManager.playerState.collectAsStateWithLifecycle()
    var musicExpanded by rememberSaveable { mutableStateOf(false) }

    // The manager restores the last session's track (paused) at startup. Only
    // surface the mini player once something has actually played this session.
    var musicSessionActive by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(musicPlayerState.isPlaying) {
        if (musicPlayerState.isPlaying) musicSessionActive = true
    }
    // The shell leaves composition while a player covers it; this keeps its pages' scroll and saved state,
    // and the played card takes focus again when it comes back.
    val shellState = rememberSaveableStateHolder()
    val returnFocus = remember { DpadReturnFocus() }

    fun play(video: Video) {
        returnFocus.key = video.id
        EnhancedMusicPlayerManager.pause()
        GlobalPlayerState.setCurrentVideo(video)
        playerViewModel.playVideo(video)
    }

    fun playPlaylist(
        videos: List<Video>,
        startIndex: Int,
        title: String,
        shuffle: Boolean,
    ) {
        val start = videos.getOrNull(startIndex) ?: return
        returnFocus.key = start.id
        EnhancedMusicPlayerManager.pause()
        GlobalPlayerState.setCurrentVideo(start)
        playerViewModel.playPlaylist(videos, startIndex, title, shuffle)
    }

    // Metadata is resolved by playVideo -> loadVideoInfo; the stub only seeds the id.
    fun playById(
        videoId: String,
        short: Boolean = false,
    ) = play(
        Video(
            id = videoId,
            title = "",
            channelName = "",
            channelId = "",
            thumbnailUrl = "",
            duration = 0,
            viewCount = 0L,
            uploadDate = "",
            isShort = short,
        ),
    )

    fun closePlayer() {
        playerViewModel.clearVideo()
        GlobalPlayerState.setCurrentVideo(null)
    }

    // A page asked for before the shell's NavHost has set its graph, as from a player a link opened.
    var pendingRoute by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(pendingRoute) {
        val route = pendingRoute ?: return@LaunchedEffect
        navController.currentBackStackEntryFlow.first()
        navController.navigate(route)
        pendingRoute = null
    }
    val mediaNavigator =
        remember(navController) {
            TvMediaNavigator(
                navController = navController,
                beforeNavigate = {
                    musicExpanded = false
                    if (GlobalPlayerState.currentVideo.value != null) closePlayer()
                },
                playVideo = { playById(it) },
                navigateWhenReady = { pendingRoute = it },
            )
        }

    LaunchedEffect(deeplinkVideoId) {
        val videoId = deeplinkVideoId ?: return@LaunchedEffect
        playById(videoId, isShort)
        onDeeplinkConsumed()
    }

    // Handoffs from other devices: the consent question, who took over, and the devices list.
    PlayOnHost(onManageDevices = { mediaNavigator.openSettings(SettingsTarget(SettingsDestination.SYNC)) })

    val musicMenus = rememberMusicMenus()

    FlowTvTheme {
        CompositionLocalProvider(
            *mediaNavigationLocals(mediaNavigator),
            LocalDpadReturnFocus provides returnFocus,
            LocalFlowSheetPresenter provides TvSheetPresenter,
            LocalMusicMenus provides musicMenus,
        ) {
            ProvideMusicPlaybackState(surfacesVisible = !musicExpanded && activeVideo == null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    val video = activeVideo
                    val visibleMusicTrack = activeMusicTrack.takeIf { musicSessionActive }
                    if (video == null && musicExpanded && visibleMusicTrack != null) {
                        TvMusicNowPlaying(
                            track = visibleMusicTrack,
                            onCollapse = { musicExpanded = false },
                        )
                    } else if (video == null) {
                        val tvNavigation = navigation ?: return@Surface
                        shellState.SaveableStateProvider(SHELL_STATE_KEY) {
                            TvShell(
                                navigation = tvNavigation,
                                navController = navController,
                                homeViewModel = homeViewModel,
                                musicViewModel = musicViewModel,
                                musicPlayerViewModel = musicPlayerViewModel,
                                subscriptionsViewModel = subscriptionsViewModel,
                                searchViewModel = searchViewModel,
                                onPlayVideo = ::play,
                                onPlayPlaylist = ::playPlaylist,
                                onPlayVideoId = { playById(it) },
                                activeMusicTrack = visibleMusicTrack,
                                onExpandMusic = { musicExpanded = true },
                                startSettings = startSettings,
                                modifier =
                                    Modifier.onPreviewKeyEvent { event ->
                                        // Any key after coming back means the remote has moved on.
                                        if (event.type == KeyEventType.KeyDown) returnFocus.key = null
                                        false
                                    },
                                onDismissMusic = {
                                    musicExpanded = false
                                    musicSessionActive = false
                                    EnhancedMusicPlayerManager.clearCurrentTrack()
                                },
                            )
                        }
                    } else {
                        TvPlayerScreen(
                            video = video,
                            viewModel = playerViewModel,
                            onClose = ::closePlayer,
                            onOpenChannel = mediaNavigator::openChannel,
                        )
                    }
                }
            }
            MusicMenuSheets(musicMenus)
        }
    }
}

private const val SHELL_STATE_KEY = "tv-shell"
