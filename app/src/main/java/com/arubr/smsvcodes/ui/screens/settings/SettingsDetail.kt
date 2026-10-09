package com.arubr.smsvcodes.ui.screens.settings

import androidx.compose.runtime.Composable
import com.arubr.smsvcodes.ui.components.settings.SettingsDestination
import com.arubr.smsvcodes.ui.components.settings.SettingsTarget
import com.arubr.smsvcodes.ui.screens.equalizer.EqualizerScreen
import com.arubr.smsvcodes.ui.screens.settings.about.AboutScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.AppearanceScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.DateTimeScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.FontScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.NavigationBarScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.player.PlayerAppearanceScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.theme.CustomThemeEditorScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.theme.CustomThemesScreen
import com.arubr.smsvcodes.ui.screens.settings.appearance.theme.ThemeScreen
import com.arubr.smsvcodes.ui.screens.settings.backup.BackupScreen
import com.arubr.smsvcodes.ui.screens.settings.content.ContentSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.content.MusicHomeSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.diagnostics.DiagnosticsScreen
import com.arubr.smsvcodes.ui.screens.settings.downloads.DownloadSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.history.HistorySettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.integrations.IntegrationsScreen
import com.arubr.smsvcodes.ui.screens.settings.localmedia.LocalMediaSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.network.NetworkSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.notifications.NotificationSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.playback.BufferSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.playback.PlaybackSettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.quality.QualitySettingsScreen
import com.arubr.smsvcodes.ui.screens.settings.region.LanguageRegionScreen
import com.arubr.smsvcodes.ui.screens.settings.scrobbling.ScrobblingScreen
import com.arubr.smsvcodes.ui.screens.settings.taste.DiscoverChannelsScreen
import com.arubr.smsvcodes.ui.screens.settings.taste.FavouriteArtistsScreen
import com.arubr.smsvcodes.ui.screens.settings.taste.HiddenContentScreen
import com.arubr.smsvcodes.ui.screens.settings.taste.TasteScreen
import com.arubr.smsvcodes.ui.screens.settings.topics.TopicPreferencesScreen
import com.arubr.smsvcodes.ui.screens.settings.wellbeing.WellbeingScreen
import com.arubr.smsvcodes.ui.screens.settings.widgets.WidgetsSettingsScreen
import com.arubr.smsvcodes.ui.screens.sync.SyncScreen

/**
 * The page a [SettingsTarget] opens. [onBack] is null for the root page beside the settings list,
 * where there is nothing to go back to; [onNavigate] opens a sub-page over this one.
 */
@Composable
internal fun SettingsDetail(
    target: SettingsTarget,
    onBack: (() -> Unit)?,
    onNavigate: (SettingsTarget) -> Unit,
    onOpenRecap: () -> Unit,
) {
    when (target.destination) {
        SettingsDestination.HOME,
        SettingsDestination.APPEARANCE,
        -> {
            AppearanceScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.TASTE -> {
            TasteScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate, onOpenRecap = onOpenRecap)
        }

        SettingsDestination.HIDDEN_CONTENT -> {
            HiddenContentScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.FAVOURITE_ARTISTS -> {
            FavouriteArtistsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.DISCOVER_CHANNELS -> {
            DiscoverChannelsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.THEME -> {
            ThemeScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.FONT -> {
            FontScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.CUSTOM_THEME -> {
            CustomThemesScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.CUSTOM_THEME_EDIT -> {
            CustomThemeEditorScreen(
                themeId = target.tab,
                onBack = onBack,
                highlight = target.highlight,
            )
        }

        SettingsDestination.NAVIGATION_BAR -> {
            NavigationBarScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.CONTENT -> {
            ContentSettingsScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.MUSIC_HOME -> {
            MusicHomeSettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.DATE_TIME -> {
            DateTimeScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.PLAYER_APPEARANCE -> {
            PlayerAppearanceScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.LANGUAGE_REGION -> {
            LanguageRegionScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.PLAYBACK -> {
            PlaybackSettingsScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.BUFFER -> {
            BufferSettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.EQUALIZER -> {
            EqualizerScreen(onBack = onBack, inSettings = true)
        }

        SettingsDestination.QUALITY -> {
            QualitySettingsScreen(onBack = onBack, highlight = target.highlight, tab = target.tab)
        }

        SettingsDestination.TOPICS -> {
            TopicPreferencesScreen(onBack = onBack, highlight = target.highlight, tab = target.tab)
        }

        SettingsDestination.INTEGRATIONS -> {
            IntegrationsScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.SCROBBLING -> {
            ScrobblingScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.BACKUP -> {
            BackupScreen(onBack = onBack, highlight = target.highlight, tab = target.tab)
        }

        SettingsDestination.SYNC -> {
            SyncScreen(onBack = onBack)
        }

        SettingsDestination.HISTORY -> {
            HistorySettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.DOWNLOADS -> {
            DownloadSettingsScreen(onBack = onBack, highlight = target.highlight, onNavigate = onNavigate)
        }

        SettingsDestination.LOCAL_MEDIA -> {
            LocalMediaSettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.NOTIFICATIONS -> {
            NotificationSettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.NETWORK -> {
            NetworkSettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.WELLBEING -> {
            WellbeingScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.WIDGETS -> {
            WidgetsSettingsScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.ABOUT -> {
            AboutScreen(onBack = onBack, highlight = target.highlight)
        }

        SettingsDestination.DIAGNOSTICS -> {
            DiagnosticsScreen(onBack = onBack, highlight = target.highlight)
        }
    }
}
