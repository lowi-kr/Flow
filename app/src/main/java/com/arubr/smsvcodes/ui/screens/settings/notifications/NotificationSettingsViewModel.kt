package com.arubr.smsvcodes.ui.screens.settings.notifications

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.arubr.smsvcodes.BuildConfig
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.notification.BackgroundWorkPolicy
import com.arubr.smsvcodes.notification.SubscriptionCheckWorker
import com.arubr.smsvcodes.notification.UpdateCheckWorker
import com.arubr.smsvcodes.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val preferences: PlayerPreferences,
    ) : SettingsViewModel() {
        val enabled = preferences.notificationsEnabled.asState(true)
        val newVideos = preferences.notifNewVideosEnabled.asState(true)
        val downloads = preferences.notifDownloadsEnabled.asState(true)
        val reminders = preferences.notifRemindersEnabled.asState(true)
        val updates = preferences.notifUpdatesEnabled.asState(true)
        val general = preferences.notifGeneralEnabled.asState(true)
        val intervalMinutes = preferences.subscriptionCheckIntervalMinutes.asState(DEFAULT_INTERVAL_MINUTES)

        private val _backgroundAllowed = MutableStateFlow(BackgroundWorkPolicy.isBackgroundWorkUnrestricted(context))
        val backgroundAllowed: StateFlow<Boolean> = _backgroundAllowed.asStateFlow()

        /** Re-reads the battery exemption; a newly granted one runs the check the restriction held back. */
        fun refreshBackgroundWork() {
            val allowed = BackgroundWorkPolicy.isBackgroundWorkUnrestricted(context)
            if (allowed && !_backgroundAllowed.value) SubscriptionCheckWorker.runImmediateCheck(context)
            _backgroundAllowed.value = allowed
        }

        fun setEnabled(value: Boolean) =
            write {
                preferences.setNotificationsEnabled(value)
                if (value) {
                    SubscriptionCheckWorker.schedulePeriodicCheck(
                        context,
                        intervalMinutes = preferences.subscriptionCheckIntervalMinutes.first().toLong(),
                        reschedule = true,
                    )
                    if (BuildConfig.UPDATER_ENABLED) UpdateCheckWorker.schedulePeriodicCheck(context, reschedule = true)
                } else {
                    SubscriptionCheckWorker.cancelScheduledChecks(context)
                    UpdateCheckWorker.cancelScheduledChecks(context)
                }
            }

        fun setInterval(minutes: Int) =
            write {
                preferences.setSubscriptionCheckIntervalMinutes(minutes)
                SubscriptionCheckWorker.schedulePeriodicCheck(context, intervalMinutes = minutes.toLong(), reschedule = true)
            }

        fun setNewVideos(value: Boolean) = write { preferences.setNotifNewVideosEnabled(value) }

        fun setDownloads(value: Boolean) = write { preferences.setNotifDownloadsEnabled(value) }

        fun setReminders(value: Boolean) = write { preferences.setNotifRemindersEnabled(value) }

        fun setUpdates(value: Boolean) = write { preferences.setNotifUpdatesEnabled(value) }

        fun setGeneral(value: Boolean) = write { preferences.setNotifGeneralEnabled(value) }

        companion object {
            const val DEFAULT_INTERVAL_MINUTES = 360
        }
    }
