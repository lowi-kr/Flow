package com.arubr.smsvcodes.ui.screens.music

import com.arubr.smsvcodes.data.local.PlaylistRepository
import com.arubr.smsvcodes.data.music.model.PlaylistDetails
import com.arubr.smsvcodes.ui.screens.music.collection.toStoredVideo

/** Saves an album or playlist to the library with its tracks, so it plays offline from the library. */
internal suspend fun PlaylistRepository.saveMusicCollection(details: PlaylistDetails) {
    saveExternalMusicPlaylist(
        id = details.id,
        name = details.title,
        description = details.description.orEmpty(),
        thumbnailUrl = details.thumbnailUrl,
    )
    addVideosToPlaylist(details.id, details.tracks.map { it.toStoredVideo() })
}
