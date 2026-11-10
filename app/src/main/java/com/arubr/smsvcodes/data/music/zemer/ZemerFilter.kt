package com.arubr.smsvcodes.data.music.zemer

import android.content.Context
import android.util.Log
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.data.music.model.ArtistDetails
import com.arubr.smsvcodes.data.music.model.MusicPlaylist
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.innertube.models.AlbumItem
import com.arubr.smsvcodes.innertube.models.ArtistItem
import com.arubr.smsvcodes.innertube.models.PlaylistItem
import com.arubr.smsvcodes.innertube.models.SongItem
import com.arubr.smsvcodes.innertube.models.YTItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Music-only allow-list for Zemer-curated artists. Off by default.
 * When on, a song/artist passes only if one of its channel ids is on the list.
 *
 * content.zemer.io/whitelist, the server endpoint this used to fetch from, has required a Zemer
 * partner key for programmatic access since 2026-10-28 and otherwise returns Cloudflare's block
 * page - confirmed by hitting it directly in a browser, not just from this app, so it isn't
 * something this app can work around. Flow has no such key. Instead this ships a snapshot of
 * YouTube channel ids, matched by hand against zemer.io's own public /artists page (R.raw.zemer_whitelist,
 * res/raw/zemer_whitelist.txt), bundled in the APK: one id per line, no network call. It goes
 * stale as Zemer's catalog changes and carries whatever matching error the snapshot had when it
 * was built - see res/raw/zemer_whitelist.txt's own history for how it was produced - but it
 * works offline and immediately, where the live endpoint currently cannot work at all.
 */
object ZemerFilter {
    private const val TAG = "ZemerFilter"

    private val _enabled = MutableStateFlow(false)
    val enabledFlow: StateFlow<Boolean> = _enabled.asStateFlow()

    @Volatile
    var enabled: Boolean = false
        private set

    @Volatile
    private var loaded: Boolean = false

    @Volatile
    private var allowed: Set<String> = emptySet()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start(context: Context, preferences: PlayerPreferences) {
        val app = context.applicationContext
        scope.launch { loadBundledWhitelist(app) }
        scope.launch {
            preferences.zemerFilterEnabled.distinctUntilChanged().collect { on ->
                enabled = on
                _enabled.value = on
            }
        }
    }

    fun allowsTrack(track: MusicTrack): Boolean {
        if (!enabled) return true
        if (!loaded) return false
        val ids = track.artists.mapNotNull { it.id }.filter { it.isNotBlank() } + listOfNotNull(track.channelId.takeIf { it.isNotBlank() })
        if (ids.isEmpty()) return false
        return ids.any { it in allowed }
    }

    /** User playlists have no author id and stay visible. Catalog rows with an author must match. */
    fun allowsPlaylist(playlist: MusicPlaylist): Boolean {
        if (!enabled) return true
        val id = playlist.authorId
        if (id.isNullOrBlank()) return true
        if (!loaded) return false
        return id in allowed
    }

    fun allowsArtist(channelId: String?): Boolean {
        if (!enabled) return true
        if (channelId.isNullOrBlank()) return false
        if (!loaded) return false
        return channelId in allowed
    }

    fun filterTracks(tracks: List<MusicTrack>): List<MusicTrack> =
        if (!enabled) tracks else tracks.filter(::allowsTrack)

    fun filterPlaylists(playlists: List<MusicPlaylist>): List<MusicPlaylist> =
        if (!enabled) playlists else playlists.filter(::allowsPlaylist)

    fun filterArtists(artists: List<ArtistDetails>): List<ArtistDetails> =
        if (!enabled) artists else artists.filter { allowsArtist(it.channelId) }

    /**
     * Covers raw Innertube search/browse results (Home feed shelves, the Search tab) before
     * they're converted into MusicTrack/MusicPlaylist/ArtistDetails - those conversions run
     * through filterTracks/filterPlaylists/filterArtists on their own, but several call sites
     * (MusicRecommendationAlgorithm's home-section and recommendation parsing, the Search
     * screen) work with YTItem directly and need this instead.
     */
    fun allowsYTItem(item: YTItem): Boolean {
        if (!enabled) return true
        if (!loaded) return false
        val ids =
            when (item) {
                is SongItem -> item.artists.mapNotNull { it.id }
                is AlbumItem -> item.artists.orEmpty().mapNotNull { it.id }
                is PlaylistItem -> listOfNotNull(item.author?.id)
                is ArtistItem -> listOfNotNull(item.channelId ?: item.id)
            }.filter { it.isNotBlank() }
        // A playlist with no attributed author (a user/auto-generated playlist, not a catalog
        // row) isn't something the whitelist can judge - matches allowsPlaylist's own rule.
        if (ids.isEmpty()) return item is PlaylistItem
        return ids.any { it in allowed }
    }

    fun filterYTItems(items: List<YTItem>): List<YTItem> =
        if (!enabled) items else items.filter(::allowsYTItem)

    private suspend fun loadBundledWhitelist(context: Context) {
        try {
            val ids =
                withContext(Dispatchers.IO) {
                    context.resources
                        .openRawResource(R.raw.zemer_whitelist)
                        .bufferedReader()
                        .useLines { lines -> lines.map(String::trim).filter(String::isNotEmpty).toHashSet() }
                }
            if (ids.isEmpty()) error("bundled whitelist was empty")
            allowed = ids
            loaded = true
            Log.d(TAG, "loaded bundled whitelist: ${ids.size} channel ids")
        } catch (e: Exception) {
            Log.e(TAG, "failed to load bundled whitelist - filter stays closed", e)
        }
    }
}
