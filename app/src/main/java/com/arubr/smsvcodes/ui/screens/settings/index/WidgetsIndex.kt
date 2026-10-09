package com.arubr.smsvcodes.ui.screens.settings.index

import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.ui.components.settings.SettingEntry
import com.arubr.smsvcodes.ui.components.settings.SettingsDestination
import com.arubr.smsvcodes.widget.core.FlowWidgetEntry
import com.arubr.smsvcodes.widget.core.FlowWidgets

internal object WidgetsIndex {
    fun entryFor(widget: FlowWidgetEntry) =
        SettingEntry(
            key = "widgets.${widget.id}",
            title = widget.label,
            summary = widget.description,
            section = R.string.settings_widgets_add_header,
            keywords = R.string.settings_keywords_widgets,
            destination = SettingsDestination.WIDGETS,
        )

    val all = FlowWidgets.catalog.map(::entryFor)
}
