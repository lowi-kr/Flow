package com.arubr.smsvcodes.ui.screens.music

import com.arubr.smsvcodes.data.model.distinctByNonBlankKey
import com.arubr.smsvcodes.data.music.zemer.ZemerFilter
import com.arubr.smsvcodes.innertube.models.AlbumItem
import com.arubr.smsvcodes.innertube.models.ArtistItem
import com.arubr.smsvcodes.innertube.models.PlaylistItem
import com.arubr.smsvcodes.innertube.models.SongItem
import com.arubr.smsvcodes.innertube.models.YTItem
import com.arubr.smsvcodes.innertube.pages.SearchSummaryPage

internal fun YTItem.stableIdentityKey(): String {
    if (id.isBlank()) return ""
    return when (this) {
        is SongItem -> "song:$id"
        is AlbumItem -> "album:$id"
        is PlaylistItem -> "playlist:$id"
        is ArtistItem -> "artist:$id"
    }
}

internal fun YTItem.stableLazyKey(namespace: String): String = "$namespace:${stableIdentityKey()}"

internal fun Iterable<YTItem>.distinctByStableIdentity(): List<YTItem> = distinctByNonBlankKey(YTItem::stableIdentityKey)

/** Drops every search result not on the Zemer whitelist, section by section, before dedup runs. */
internal fun SearchSummaryPage.filteredByZemer(): SearchSummaryPage =
    copy(
        summaries =
            summaries.mapNotNull { summary ->
                summary.copy(items = ZemerFilter.filterYTItems(summary.items)).takeIf { it.items.isNotEmpty() }
            },
    )

internal fun SearchSummaryPage.distinctItemsForLazyKeys(): SearchSummaryPage {
    val seenBySection = mutableMapOf<String, MutableSet<String>>()
    return copy(
        summaries =
            summaries.mapNotNull { summary ->
                val seenKeys = seenBySection.getOrPut("${summary.kind}:${summary.title}") { HashSet() }
                summary
                    .copy(
                        items =
                            summary.items.filter { item ->
                                val key = item.stableIdentityKey()
                                item.id.isNotBlank() && seenKeys.add(key)
                            },
                    ).takeIf { it.items.isNotEmpty() }
            },
    )
}
