package com.arubr.smsvcodes.ui.screens.settings.index

import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.ui.components.music.section.MusicHomeShelf
import com.arubr.smsvcodes.ui.components.settings.SettingEntry
import com.arubr.smsvcodes.ui.components.settings.SettingsDestination

internal object MusicHomeIndex {
    private val entries: Map<MusicHomeShelf, SettingEntry> =
        MusicHomeShelf.entries.associateWith { shelf ->
            SettingEntry(
                key = "music_home.${shelf.name.lowercase()}",
                title = shelf.labelRes,
                section = R.string.settings_music_home_sections,
                destination = SettingsDestination.MUSIC_HOME,
            )
        }

    fun entry(shelf: MusicHomeShelf): SettingEntry = entries.getValue(shelf)

    val all: List<SettingEntry> = MusicHomeShelf.entries.map(::entry)
}
