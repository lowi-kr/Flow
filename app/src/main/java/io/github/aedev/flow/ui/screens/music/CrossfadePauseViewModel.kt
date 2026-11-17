package com.arubr.smsvcodes.ui.screens.music

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.player.crossfade.CrossfadeSessionPause
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Backs the music player menu's "Pause crossfade" row, shown only while crossfade is on in settings. */
@HiltViewModel
class CrossfadePauseViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val pause: CrossfadeSessionPause,
    ) : ViewModel() {
        val crossfadeOn: StateFlow<Boolean> =
            PlayerPreferences(context).musicCrossfadeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        val paused: StateFlow<Boolean> = pause.paused

        fun toggle() = pause.toggle()
    }
