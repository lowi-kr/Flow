package com.arubr.smsvcodes.notification

import androidx.work.ExistingPeriodicWorkPolicy

internal fun periodicWorkPolicy(reschedule: Boolean): ExistingPeriodicWorkPolicy =
    if (reschedule) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP
