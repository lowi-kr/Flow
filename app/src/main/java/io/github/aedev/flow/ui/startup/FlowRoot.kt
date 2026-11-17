package com.arubr.smsvcodes.ui.startup

import android.os.Build
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arubr.smsvcodes.applock.AppLock
import com.arubr.smsvcodes.data.contentfilter.aislist.AiListRepository
import com.arubr.smsvcodes.data.local.AppUiModePreferences
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.data.recommendation.FlowNeuroEngine
import com.arubr.smsvcodes.data.repository.DeArrowRepository
import com.arubr.smsvcodes.data.repository.OriginalTitleRepository
import com.arubr.smsvcodes.platform.AppIconController
import com.arubr.smsvcodes.platform.AppUiMode
import com.arubr.smsvcodes.platform.AppUiRoot
import com.arubr.smsvcodes.platform.DeviceFormFactorDetector
import com.arubr.smsvcodes.player.GlobalPlayerState
import com.arubr.smsvcodes.ui.FlowApp
import com.arubr.smsvcodes.ui.IncomingLinks
import com.arubr.smsvcodes.ui.components.applock.AppLockScreen
import com.arubr.smsvcodes.ui.components.profiles.ProfileSwitchingOverlay
import com.arubr.smsvcodes.ui.components.shared.AppHaptics
import com.arubr.smsvcodes.ui.components.shared.ProvideChannelGroupLabels
import com.arubr.smsvcodes.ui.components.shared.ProvideChannelOrganizer
import com.arubr.smsvcodes.ui.components.shared.ProvideDateDisplaySettings
import com.arubr.smsvcodes.ui.components.shared.card.ProvideVideoCardState
import com.arubr.smsvcodes.ui.screens.crash.CrashReportScreen
import com.arubr.smsvcodes.ui.screens.playon.LocalPlayOn
import com.arubr.smsvcodes.ui.screens.playon.PlayOnViewModel
import com.arubr.smsvcodes.ui.theme.FlowFontFamily
import com.arubr.smsvcodes.ui.tv.FlowTvApp
import com.arubr.smsvcodes.ui.tv.screens.TvProfileGate
import com.arubr.smsvcodes.ui.utils.ProvideWindowSizeClass
import com.arubr.smsvcodes.utils.FlowCrashHandler
import kotlinx.coroutines.flow.StateFlow

/**
 * Everything MainActivity shows: the theme gate, the phone or TV app, a crash report from the last session, and the
 * providers every screen reads. [onUiRoot] reports which of phone or TV is showing.
 */
@Composable
internal fun FlowRoot(
    storedTheme: StateFlow<ThemeSettings?>,
    links: IncomingLinks,
    splashController: SplashController,
    originalTitles: OriginalTitleRepository,
    deArrow: DeArrowRepository,
    aiList: AiListRepository,
    appIconController: AppIconController,
    appLock: AppLock,
    onUiRoot: (AppUiRoot) -> Unit,
) {
    // Nothing is composed until the theme is known: the splash covers the wait, and the app
    // composes once in the right theme instead of twice. The same for the app lock, so nothing
    // shows for a frame before it.
    val theme = storedTheme.collectAsState().value ?: return
    val locked = appLock.locked.collectAsState().value ?: return
    val isInPip by GlobalPlayerState.isInPipMode.collectAsState()

    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val configuration = LocalConfiguration.current
    val uiPreferences = remember { AppUiModePreferences(applicationContext) }
    val appUiMode by uiPreferences.mode.collectAsState(initial = AppUiMode.AUTOMATIC)
    val hapticPreferences = remember { PlayerPreferences(applicationContext) }
    val hapticsEnabled by hapticPreferences.hapticsEnabled.collectAsState(initial = true)
    val hapticsIgnoreSystem by hapticPreferences.hapticsIgnoreSystem.collectAsState(initial = false)
    val deviceFormFactor =
        remember(configuration.uiMode, context) {
            DeviceFormFactorDetector.detect(context)
        }
    val appUiRoot = appUiMode.resolve(deviceFormFactor)
    SideEffect { onUiRoot(appUiRoot) }

    // Check for a crash that happened last session.
    // If found, show the CrashReportScreen instead of the normal UI. It is cleared as soon
    // as it is read, so leaving the screen any way shows it once, not on every launch.
    var pendingCrashLog by remember {
        mutableStateOf(
            FlowCrashHandler.getLastCrash(applicationContext)?.also {
                FlowCrashHandler.clearLastCrash(applicationContext)
            },
        )
    }

    if (pendingCrashLog != null) {
        SideEffect { splashController.contentReady = true }
        FlowTheme(theme.copy(fontFamily = FlowFontFamily)) {
            Box(Modifier.fillMaxSize()) {
                CrashReportScreen(
                    report = pendingCrashLog!!,
                    onContinue = { pendingCrashLog = null },
                )
                if (locked) AppLockScreen(appLock)
            }
        }
        return
    }

    // Initialize Flow Neuro Engine
    LaunchedEffect(Unit) {
        FlowNeuroEngine.initialize(applicationContext)
    }

    FlowTheme(theme) {
        val splashTone = splashTone(MaterialTheme.colorScheme.background)
        val selectedIcon by appIconController.selected.collectAsState(initial = null)
        LaunchedEffect(splashTone, selectedIcon) { splashController.rememberTheme(splashTone, appIconController.activeSuffix()) }

        // Date preferences: five DataStore flows used to be opened per video card,
        // metadata line, info section, description sheet and info dialog.
        AppHaptics(enabled = hapticsEnabled, ignoreSystemSetting = hapticsIgnoreSystem) {
            ProvideWindowSizeClass {
                ProvideDateDisplaySettings {
                    // Card preferences and watch progress are collected once here. Cards used to
                    // collect them individually, so a feed of ten opened ten Room observers and
                    // fifty DataStore collectors.
                    ProvideVideoCardState(originalTitles, deArrow, aiList) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .semantics { testTagsAsResourceId = true },
                        ) {
                            // PiP shows only what is already playing, so the lock waits for the full app.
                            val showLock = locked && !isInPip
                            Box(Modifier.fillMaxSize().hiddenBehindLock(showLock)) {
                                if (appUiRoot == AppUiRoot.MOBILE) PreAndroid9FocusHolder()
                                // 1. MAIN APP (Home/NavHost)
                                // This loads *behind* the splash screen immediately.
                                // By the time splash fades, this is ready.
                                val deeplinkVideoId by links.videoId
                                val isDeeplinkShort by links.isShort
                                val isDeeplinkShared by links.isShared
                                val openMusicPlayerRequest by links.musicPlayerRequest
                                val pendingRoute by links.route
                                val downloadVideoId by links.downloadVideoId

                                ProvideChannelGroupLabels {
                                    // One "Play on" for every player and both shells.
                                    CompositionLocalProvider(LocalPlayOn provides hiltViewModel<PlayOnViewModel>()) {
                                        if (appUiRoot == AppUiRoot.TV) {
                                            SideEffect { splashController.contentReady = true }
                                            TvProfileGate(hasLink = deeplinkVideoId != null) { startSettings ->
                                                FlowTvApp(
                                                    deeplinkVideoId = deeplinkVideoId,
                                                    isShort = isDeeplinkShort,
                                                    onDeeplinkConsumed = links::consumeVideo,
                                                    startSettings = startSettings,
                                                )
                                            }
                                        } else {
                                            ProvideChannelOrganizer {
                                                FlowApp(
                                                    currentTheme = theme.themeMode,
                                                    themeVariant = theme.themeVariant,
                                                    systemLightThemeMode = theme.systemLightThemeMode,
                                                    systemDarkThemeMode = theme.systemDarkThemeMode,
                                                    deeplinkVideoId = deeplinkVideoId,
                                                    isShort = isDeeplinkShort,
                                                    isSharedLink = isDeeplinkShared,
                                                    openMusicPlayerRequest = openMusicPlayerRequest,
                                                    onDeeplinkConsumed = links::consumeVideo,
                                                    pendingRoute = pendingRoute,
                                                    onPendingRouteConsumed = links::consumeRoute,
                                                    downloadVideoId = downloadVideoId,
                                                    onDownloadConsumed = links::consumeDownload,
                                                    onStartDestinationKnown = { splashController.contentReady = true },
                                                )
                                            }
                                        }
                                    }
                                }
                                ProfileSwitchingOverlay(
                                    currentTone = splashTone.name,
                                    onSplashTone = { name ->
                                        SplashTone.entries.firstOrNull { it.name == name }?.let {
                                            splashController.rememberTheme(
                                                it,
                                                appIconController.activeSuffix(),
                                            )
                                        }
                                    },
                                )
                            }
                            if (showLock) AppLockScreen(appLock)
                        }
                    }
                }
            }
        }
    }
}

/** While locked, the app underneath stays composed but can't be read out, focused or reached. */
private fun Modifier.hiddenBehindLock(hidden: Boolean): Modifier =
    if (hidden) {
        clearAndSetSemantics {}
            .focusProperties { onEnter = { cancelFocusChange() } }
            .focusGroup()
    } else {
        this
    }

/**
 * Before Android 9, clearing focus makes the platform hand it straight to the first focusable node from the top, so a
 * search field took it back and reopened typing (#1695). This node takes it instead. It needs a size because the
 * platform's search goes by bounds; a zero-size target is passed over.
 */
@Composable
private fun PreAndroid9FocusHolder() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) Box(Modifier.size(1.dp).focusTarget())
}
