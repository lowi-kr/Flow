package com.arubr.smsvcodes.ui.screens.music

import com.google.common.truth.Truth.assertThat
import com.arubr.smsvcodes.data.music.model.MusicArtist
import com.arubr.smsvcodes.innertube.models.Artist
import com.arubr.smsvcodes.innertube.models.SongItem
import org.junit.Test

class SearchSongArtistsTest {
    @Test
    fun `a searched song keeps every credited artist with its id`() {
        val track =
            convertSongToMusicTrack(
                SongItem(
                    id = "straight-up",
                    title = "Straight Up",
                    artists = listOf(Artist("Karan Aujla", "UCka"), Artist("Mxrci", "UCmxrci")),
                    thumbnail = "",
                ),
            )

        assertThat(track.artist).isEqualTo("Karan Aujla, Mxrci")
        assertThat(track.channelId).isEqualTo("UCka")
        assertThat(track.artists).containsExactly(MusicArtist("Karan Aujla", "UCka"), MusicArtist("Mxrci", "UCmxrci")).inOrder()
    }
}
