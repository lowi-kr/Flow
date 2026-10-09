package com.arubr.smsvcodes.widget.onrepeat

import androidx.glance.appwidget.GlanceAppWidget
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.data.recommendation.music.MusicBrainEngine
import com.arubr.smsvcodes.data.recommendation.music.onRepeatShelf
import com.arubr.smsvcodes.widget.core.refresh.WidgetContentKey
import com.arubr.smsvcodes.widget.core.refresh.WidgetContentSource
import javax.inject.Inject
import javax.inject.Singleton

/** The Music page's On Repeat shelf. The brain has no change stream, so a finished listen requests a refresh. */
@Singleton
class OnRepeatSource
    @Inject
    constructor(
        private val musicBrain: MusicBrainEngine,
    ) : WidgetContentSource {
        override val key = WidgetContentKey.ON_REPEAT
        override val widget: GlanceAppWidget get() = OnRepeatWidget()

        suspend fun tracks(): List<MusicTrack> = musicBrain.onRepeatShelf().take(MAX_ITEMS)

        override suspend fun signature(): String = tracks().joinToString(",") { it.videoId }

        companion object {
            const val MAX_ITEMS = 8
        }
    }
