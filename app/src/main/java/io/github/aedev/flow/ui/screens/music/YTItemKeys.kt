package com.arubr.smsvcodes.ui.screens.music

import com.arubr.smsvcodes.innertube.models.AlbumItem
import com.arubr.smsvcodes.innertube.models.ArtistItem
import com.arubr.smsvcodes.innertube.models.PlaylistItem
import com.arubr.smsvcodes.innertube.models.SongItem
import com.arubr.smsvcodes.innertube.models.YTItem

internal fun YTItem.stableLazyKey(namespace: String): String = when (this) {
    is SongItem -> "$namespace:song:$id"
    is AlbumItem -> "$namespace:album:$id"
    is PlaylistItem -> "$namespace:playlist:$id"
    is ArtistItem -> "$namespace:artist:$id"
}
