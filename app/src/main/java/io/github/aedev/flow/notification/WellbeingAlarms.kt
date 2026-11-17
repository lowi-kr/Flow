package com.arubr.smsvcodes.notification

import android.content.Context
import com.arubr.smsvcodes.data.local.LocalDataManager
import kotlinx.coroutines.flow.first

/**
 * The bedtime reminder of the running profile. Alarms outlive the process, so a switch cancels it and the next profile
 * arms its own from its settings.
 */
internal object WellbeingAlarms {
    suspend fun arm(
        context: Context,
        settings: LocalDataManager,
    ) {
        if (settings.bedtimeReminder.first()) {
            ReminderManager.scheduleBedtimeReminder(context, settings.bedtimeStartHour.first(), settings.bedtimeStartMinute.first())
        } else {
            ReminderManager.cancelBedtimeReminder(context)
        }
    }

    fun cancel(context: Context) {
        ReminderManager.cancelBedtimeReminder(context)
    }
}
