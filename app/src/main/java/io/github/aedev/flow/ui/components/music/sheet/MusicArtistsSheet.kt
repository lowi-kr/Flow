package com.arubr.smsvcodes.ui.components.music.sheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.stringResource
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.data.music.model.MusicArtist
import com.arubr.smsvcodes.data.music.model.MusicTrack
import com.arubr.smsvcodes.data.newmusic.MusicArtistLookup
import com.arubr.smsvcodes.player.EnhancedMusicPlayerManager
import com.arubr.smsvcodes.ui.components.layout.navigation.LocalMediaNavigator
import com.arubr.smsvcodes.ui.components.shared.ChannelAvatarStack
import com.arubr.smsvcodes.ui.components.shared.FlowNavRow
import com.arubr.smsvcodes.ui.components.shared.quickactions.QuickActionRow
import com.arubr.smsvcodes.ui.components.shared.quickactions.QuickActionsDefaults
import com.arubr.smsvcodes.ui.components.shared.quickactions.QuickActionsGroup
import com.arubr.smsvcodes.ui.components.shared.quickactions.QuickActionsPageHeader
import com.arubr.smsvcodes.ui.components.shared.quickactions.QuickActionsSheet
import com.arubr.smsvcodes.ui.components.shared.quickactions.QuickActionsViewModel
import com.arubr.smsvcodes.ui.components.shared.quickactions.sharedQuickActionsViewModel

/** The song's artists with ids, or its channel when the source named no artist. */
internal fun MusicTrack.menuArtists(): List<MusicArtist> =
    artists.filter { !it.id.isNullOrBlank() }.ifEmpty {
        if (channelId.isNotBlank()) listOf(MusicArtist(name = artist, id = channelId)) else emptyList()
    }

/**
 * True when the artist line names more artists than the track has ids for, as a song from history,
 * liked songs or a playlist does: those keep "A, B" as text and only the first artist's channel.
 */
internal fun MusicTrack.namesMoreArtistsThanIds(): Boolean {
    val named = artist.split(*ARTIST_SEPARATORS).count { it.isNotBlank() }
    return named > 1 && artists.count { !it.id.isNullOrBlank() } < named
}

private val ARTIST_SEPARATORS = arrayOf(", ", " & ")

/**
 * The track's artists with ids, looked up once when its artist line names more than it has ids for.
 * The ones found are written back into the queue, so the next tap and the saved queue have them.
 */
internal suspend fun resolveTrackArtists(
    track: MusicTrack,
    lookup: MusicArtistLookup,
): List<MusicArtist> {
    val known = track.menuArtists()
    if (!track.namesMoreArtistsThanIds()) return known
    val found = lookup.artistsOf(track.videoId)
    if (found.size <= known.size) return known
    EnhancedMusicPlayerManager.updateArtists(track.videoId, found)
    return found
}

/** One row per credited artist, each opening that artist's page. */
@Composable
internal fun MusicArtistsPage(
    artists: List<MusicArtist>,
    avatars: Map<String, String>,
    onBack: (() -> Unit)?,
    onClose: () -> Unit,
) {
    QuickActionsPageHeader(title = stringResource(R.string.quick_action_artists), onBack = onBack, onClose = onClose)
    QuickActionsGroup(title = null, rows = artists.map { artistRow(it, listOfNotNull(avatars[it.id]), onClose) })
}

/** The song menu's Artists page on its own, for a song credited to several artists. */
@Composable
internal fun MusicArtistsSheet(
    artists: List<MusicArtist>,
    onDismiss: () -> Unit,
    quickActions: QuickActionsViewModel = sharedQuickActionsViewModel(),
) {
    val avatars by produceState(emptyMap<String, String>(), artists) {
        value = quickActions.channelAvatars(artists.mapNotNull { it.id })
    }
    QuickActionsSheet(onDismiss = onDismiss) { sheet ->
        MusicArtistsPage(artists = artists, avatars = avatars, onBack = null, onClose = sheet::close)
    }
}

@Composable
private fun artistRow(
    artist: MusicArtist,
    avatar: List<String>,
    onDismiss: () -> Unit,
): QuickActionRow {
    val navigator = LocalMediaNavigator.current
    val ringColor = MaterialTheme.colorScheme.surfaceContainerHigh
    return QuickActionRow(artist.id.orEmpty()) { shape ->
        FlowNavRow(
            title = artist.name,
            onClick = {
                navigator.openArtist(artist.id.orEmpty())
                onDismiss()
            },
            shape = shape,
            leadingContent = {
                ChannelAvatarStack(
                    urls = avatar,
                    contentDescription = null,
                    avatarSize = QuickActionsDefaults.AvatarSize,
                    ringColor = ringColor,
                )
            },
            trailingContent = { Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null) },
        )
    }
}
