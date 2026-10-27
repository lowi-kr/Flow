package com.arubr.smsvcodes.ui.screens.playlists

import com.arubr.smsvcodes.data.model.PlaylistInfo
import com.arubr.smsvcodes.data.playlist.PlaylistListOrder
import com.arubr.smsvcodes.data.playlist.sortedFor
import com.arubr.smsvcodes.ui.components.library.PlaylistOwnershipFilter

/**
 * Owned playlists lead saved ones in every order except [PlaylistListOrder.CUSTOM], where the
 * viewer's own arrangement decides.
 */
internal fun orderedPlaylists(
    owned: List<PlaylistInfo>,
    saved: List<PlaylistInfo>,
    filter: PlaylistOwnershipFilter,
    order: PlaylistListOrder,
): List<PlaylistInfo> =
    if (order == PlaylistListOrder.CUSTOM) {
        filter.select(owned, saved).sortedFor(order)
    } else {
        filter.select(owned.sortedFor(order), saved.sortedFor(order))
    }
