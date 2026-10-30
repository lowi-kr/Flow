package com.arubr.smsvcodes.data.music.zemer

import android.content.Context
import android.util.Log
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.data.music.model.ArtistDetails
import com.arubr.smsvcodes.data.music.model.MusicPlaylist
import com.arubr.smsvcodes.data.music.model.MusicTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Music-only allow-list from content.zemer.io. Off by default.
 * When on, a song/artist passes only if one of its channel ids is on the list and not flagged female.
 */
object ZemerFilter {
    private const val TAG = "ZemerFilter"
    private const val BASE = "https://content.zemer.io"

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
        loadCache(app)
        scope.launch {
            preferences.zemerFilterEnabled.distinctUntilChanged().collect { on ->
                enabled = on
                _enabled.value = on
                if (on) refresh(app)
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

    private fun loadCache(context: Context) {
        val file = File(context.filesDir, "zemer_allowed_ids.txt")
        if (!file.exists()) return
        val ids = file.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        if (ids.isNotEmpty()) {
            allowed = ids
            loaded = true
        }
    }

    private suspend fun refresh(context: Context) {
        // content.zemer.io blocks this endpoint outright behind Cloudflare - confirmed by hitting
        // it directly in a browser, not just from this app - so retrying is just guaranteed-failed
        // overhead, not recovery from a flaky request. One attempt, logged once, and move on; this
        // stays fail-closed (see allowsTrack/allowsArtist/allowsPlaylist) until there's a
        // legitimate source for the list.
        try {
            val body = get("$BASE/whitelist")
            val array = JSONArray(body)
            val ids = HashSet<String>(array.length())
            for (i in 0 until array.length()) {
                val doc = array.optJSONObject(i) ?: continue
                if (doc.optBoolean("isFemale", false)) continue
                val id = doc.optString("id").ifBlank { doc.optString("artistId") }.ifBlank { doc.optString("docId") }
                if (id.isNotBlank()) ids.add(id)
            }
            if (ids.isEmpty()) error("empty whitelist response")
            applyBlocked(ids)
            allowed = ids
            loaded = true
            File(context.filesDir, "zemer_allowed_ids.txt").writeText(ids.joinToString("\n"))
            Log.d(TAG, "whitelist ${ids.size} artists")
        } catch (e: Exception) {
            Log.w(TAG, "whitelist fetch failed - content.zemer.io blocks this endpoint; filter stays closed", e)
        }
    }

    private fun applyBlocked(ids: MutableSet<String>) {
        val raw = runCatching { get("$BASE/blockedContentIds") }.getOrNull() ?: return
        val root = JSONObject(raw)
        fun drop(key: String) {
            val list = root.optJSONArray(key) ?: return
            for (i in 0 until list.length()) {
                val id = list.optString(i)
                if (id.isNotBlank()) ids.remove(id)
            }
        }
        drop("female")
        drop("global")
    }

    // content.zemer.io/whitelist returns Cloudflare's "Sorry, you have been blocked" page to a
    // plain browser too, not just to this app - it's a deliberate block on that endpoint, not a
    // client-identification problem this app can fix. The real User-Agent here is honest
    // identification, not an attempt to get past that block; this still does not attempt to
    // reproduce Zemer's private app-signing header (X-Zemer-Sig), which is never in their source
    // tree and issued per build.
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "Flow/${com.arubr.smsvcodes.BuildConfig.VERSION_NAME}")
            .build()
        httpClient.newCall(request).execute().use { response ->
            val text = response.body?.string() ?: ""
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            return text
        }
    }
}
