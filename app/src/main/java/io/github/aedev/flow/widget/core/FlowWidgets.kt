package com.arubr.smsvcodes.widget.core

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.arubr.smsvcodes.widget.downloads.DownloadsWidget
import com.arubr.smsvcodes.widget.nowplaying.NowPlayingWidget
import com.arubr.smsvcodes.widget.onrepeat.OnRepeatWidget
import com.arubr.smsvcodes.widget.quickactions.QuickActionsWidget
import com.arubr.smsvcodes.widget.recent.RecentlyPlayedWidget
import com.arubr.smsvcodes.widget.recognize.RecognizeWidget
import com.arubr.smsvcodes.widget.turntable.TurntableWidget

/** Registry of every Flow widget — used to re-render all of them on app theme changes. */
object FlowWidgets {
    suspend fun updateAll(context: Context) {
        NowPlayingWidget().updateAll(context)
        TurntableWidget().updateAll(context)
        QuickActionsWidget().updateAll(context)
        RecognizeWidget().updateAll(context)
        RecentlyPlayedWidget().updateAll(context)
        DownloadsWidget().updateAll(context)
        OnRepeatWidget().updateAll(context)
    }
}
