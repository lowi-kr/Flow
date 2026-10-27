package com.arubr.smsvcodes.widget.core

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.arubr.smsvcodes.widget.core.refresh.WidgetContentSync
import com.arubr.smsvcodes.widget.downloads.DownloadsSource
import com.arubr.smsvcodes.widget.onrepeat.OnRepeatSource
import com.arubr.smsvcodes.widget.playlist.PlaylistWidgetSource
import com.arubr.smsvcodes.widget.recent.RecentlyPlayedSource
import com.arubr.smsvcodes.widget.week.WeekSource

/**
 * Glance widgets can't use constructor injection (the framework instantiates them),
 * so Hilt singletons are reached through this entry point.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun recentlyPlayedSource(): RecentlyPlayedSource

    fun downloadsSource(): DownloadsSource

    fun onRepeatSource(): OnRepeatSource

    fun playlistWidgetSource(): PlaylistWidgetSource

    fun weekSource(): WeekSource

    fun widgetContentSync(): WidgetContentSync
}

fun widgetEntryPoint(context: Context): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
