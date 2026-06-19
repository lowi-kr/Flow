package com.arubr.smsvcodes.innertube.pages

import com.arubr.smsvcodes.innertube.models.Album
import com.arubr.smsvcodes.innertube.models.AlbumItem
import com.arubr.smsvcodes.innertube.models.Artist
import com.arubr.smsvcodes.innertube.models.ArtistItem
import com.arubr.smsvcodes.innertube.models.MusicResponsiveListItemRenderer
import com.arubr.smsvcodes.innertube.models.MusicTwoRowItemRenderer
import com.arubr.smsvcodes.innertube.models.PlaylistItem
import com.arubr.smsvcodes.innertube.models.SongItem
import com.arubr.smsvcodes.innertube.models.YTItem
import com.arubr.smsvcodes.innertube.models.oddElements
import com.arubr.smsvcodes.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
