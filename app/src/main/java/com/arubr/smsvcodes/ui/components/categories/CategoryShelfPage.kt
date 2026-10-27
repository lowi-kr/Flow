package com.arubr.smsvcodes.ui.components.categories

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.arubr.smsvcodes.data.local.HomeFeedColumns
import com.arubr.smsvcodes.data.model.Video
import com.arubr.smsvcodes.innertube.pages.renderer.FeedShelf
import com.arubr.smsvcodes.ui.components.layout.flowBottomContentPadding
import com.arubr.smsvcodes.ui.components.shared.FeedShelfActions
import com.arubr.smsvcodes.ui.components.shared.FeedShelfSections

/**
 * A destination's landing page. Only shelves that can be opened get a chevron — the news
 * destination clusters its stories without publishing a "see all" for any of them.
 *
 * No post slot is passed, so the one shelf of community posts the news destination ships is left
 * out rather than rendered as a bare header.
 */
@Composable
internal fun CategoryShelfPage(
    shelves: List<FeedShelf>,
    isLoading: Boolean,
    listState: LazyGridState,
    columnPreference: HomeFeedColumns,
    onVideoClick: (Video) -> Unit,
    onShortClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onShelfOpen: (FeedShelf) -> Unit,
) {
    val actions =
        remember(onVideoClick, onShortClick, onPlaylistClick, onShelfOpen) {
            FeedShelfActions(
                onVideoClick = onVideoClick,
                onShortClick = onShortClick,
                onPlaylistClick = onPlaylistClick,
                onSectionMore = onShelfOpen,
                canOpenSection = { it.moreParams != null },
            )
        }

    FeedShelfSections(
        sections = shelves,
        isLoading = isLoading,
        listState = listState,
        columnPreference = columnPreference,
        contentPadding = PaddingValues(bottom = flowBottomContentPadding()),
        topInset = 0.dp,
        actions = actions,
        showChannelInfo = true,
    )
}
