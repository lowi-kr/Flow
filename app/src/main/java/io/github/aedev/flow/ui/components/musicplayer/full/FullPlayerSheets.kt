package com.arubr.smsvcodes.ui.components.musicplayer.full

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arubr.smsvcodes.data.music.model.MusicArtist
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.ui.components.music.sheet.CrossfadePauseRow
import com.arubr.smsvcodes.ui.components.music.sheet.MusicArtistsSheet
import com.arubr.smsvcodes.ui.components.music.sheet.MusicQuickActionsSheet
import com.arubr.smsvcodes.ui.components.music.sheet.SaveSongSheet
import com.arubr.smsvcodes.ui.components.music.sheet.SaveSongsSheet
import com.arubr.smsvcodes.ui.components.musicplayer.sheet.AudioSettingsSheet
import com.arubr.smsvcodes.ui.screens.music.CrossfadePauseViewModel
import com.arubr.smsvcodes.ui.screens.music.MusicCastViewModel

/** Which of the full player's own sheets is open. */
@Stable
internal class FullPlayerSheetState {
    var moreOptions by mutableStateOf(false)
    var save by mutableStateOf(false)
    var saveTracks by mutableStateOf(emptyList<MusicTrack>())
    var audioSettings by mutableStateOf(false)
    var sleepTimer by mutableStateOf(false)
    var artists by mutableStateOf(emptyList<MusicArtist>())
}

@Composable
internal fun FullPlayerSheets(
    state: FullPlayerSheetState,
    currentTrack: MusicTrack?,
    isPlayerSheetExpanded: Boolean,
) {
    if (state.save && currentTrack != null) {
        SaveSongSheet(
            track = currentTrack,
            onDismiss = { state.save = false },
        )
    }

    if (state.saveTracks.isNotEmpty()) {
        SaveSongsSheet(
            tracks = state.saveTracks,
            onDismiss = { state.saveTracks = emptyList() },
        )
    }

    if (state.moreOptions && currentTrack != null) {
        val crossfade: CrossfadePauseViewModel = hiltViewModel()
        val crossfadeOn by crossfade.crossfadeOn.collectAsStateWithLifecycle()
        val crossfadePaused by crossfade.paused.collectAsStateWithLifecycle()
        val cast: MusicCastViewModel = hiltViewModel()
        MusicQuickActionsSheet(
            track = currentTrack,
            onDismiss = { state.moreOptions = false },
            onAudioEffectsClick = { state.audioSettings = true },
            onSleepTimerClick = { state.sleepTimer = true },
            crossfadePause = if (crossfadeOn) CrossfadePauseRow(crossfadePaused, crossfade::toggle) else null,
            onCastClick = cast::openPicker,
        )
    }

    currentTrack?.let { MusicCastHost(title = it.title) }

    if (state.audioSettings) {
        AudioSettingsSheet(
            onDismiss = { state.audioSettings = false },
        )
    }

    // The player stays composed under the mini bar, so the artists sheet closes with it.
    LaunchedEffect(isPlayerSheetExpanded) {
        if (!isPlayerSheetExpanded) state.artists = emptyList()
    }
    if (state.artists.isNotEmpty() && isPlayerSheetExpanded) {
        MusicArtistsSheet(
            artists = state.artists,
            onDismiss = { state.artists = emptyList() },
        )
    }
}
