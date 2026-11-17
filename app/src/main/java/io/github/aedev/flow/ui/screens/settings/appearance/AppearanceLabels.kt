package com.arubr.smsvcodes.ui.screens.settings.appearance

import androidx.annotation.StringRes
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.ui.theme.ThemeVariant
import com.arubr.smsvcodes.util.AppIcons
import com.arubr.smsvcodes.util.LauncherIcon

@StringRes
internal fun themeVariantLabel(variant: ThemeVariant): Int =
    when (variant) {
        ThemeVariant.LIGHT -> R.string.appearance_variant_light
        ThemeVariant.DARK -> R.string.appearance_variant_dark
        ThemeVariant.AMOLED -> R.string.appearance_variant_amoled
    }

/** Every alias in manifest order; the order and the set come from [AppIcons], never from this file. */
internal val appIconOptions: List<LauncherIcon>
    get() = AppIcons.ALL

internal fun appIconOption(suffix: String): LauncherIcon = AppIcons.icon(suffix)
