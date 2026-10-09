package com.arubr.smsvcodes.ui.components.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.data.local.ContentType
import com.arubr.smsvcodes.data.local.Duration
import com.arubr.smsvcodes.data.local.SearchFilter
import com.arubr.smsvcodes.data.local.SortType
import com.arubr.smsvcodes.data.local.UploadDate

/** What [this] narrows, in dialog order. Channels and playlists ignore duration and date, so those are left out there. */
internal fun SearchFilter.summaryLabelRes(): List<Int> =
    buildList {
        if (contentType != ContentType.ALL) add(contentType.labelRes())
        if (contentType != ContentType.CHANNELS && contentType != ContentType.PLAYLISTS) {
            if (duration != Duration.ANY) add(duration.labelRes())
            if (uploadDate != UploadDate.ANY) add(uploadDate.labelRes())
        }
        if (sortType != SortType.RELEVANCE) add(sortType.labelRes())
        features.sorted().forEach { add(it.labelRes()) }
    }

/** One line naming a history entry's filters, or null when it ran unfiltered. */
@Composable
internal fun searchFilterSummary(filter: SearchFilter): String? {
    val labels = filter.summaryLabelRes().map { stringResource(it) }
    if (labels.isEmpty()) return null
    return labels.joinToString(stringResource(R.string.list_separator_dot))
}
