package com.arubr.smsvcodes.innertube.pages

import com.arubr.smsvcodes.innertube.models.MusicCarouselShelfRenderer
import com.arubr.smsvcodes.innertube.models.PlaylistItem
import com.arubr.smsvcodes.innertube.models.SongItem
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.ARTIST
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.ATV
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.PODCAST
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.browseEndpoint
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.carousel
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.multiRowEpisode
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.run
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.songRow
import com.arubr.smsvcodes.innertube.pages.InnerTubeJson.twoRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePageSectionTest {
    private val michael = run("Michael Jackson", browseEndpoint("UCmichael", ARTIST))

    private fun shelf(
        title: String,
        items: List<String>,
    ): MusicCarouselShelfRenderer =
        InnerTubeJson
            .browse(listOf(carousel(title, items)))
            .contents
            ?.sectionListRenderer
            ?.contents
            ?.single()
            ?.musicCarouselShelfRenderer!!

    private val quickPicks = shelf("Quick picks", listOf(songRow("s1", "Beat It", ATV, listOf(michael))))

    private val episodes =
        shelf(
            "Latest episodes",
            listOf(multiRowEpisode("e1", "Episode one", "Daily Talk"), multiRowEpisode("e2", "Episode two", "Weekly Talk")),
        )

    private val shows =
        shelf(
            "Popular shows",
            listOf(
                twoRow("MPSPPLdaily", PODCAST, "Daily Talk", listOf(run("Host", browseEndpoint("UChost", ARTIST))), playlistId = "PLdaily"),
                twoRow("MPSPPLweekly", PODCAST, "Weekly Talk", listOf(run("Podcast"))),
            ),
        )

    @Test
    fun `a chip's song rows are kept`() {
        val section = HomePage.Section.fromMusicCarouselShelfRenderer(quickPicks, chipPage = true)

        val song = section?.items?.single() as SongItem
        assertEquals("Quick picks", section.title)
        assertEquals("s1", song.id)
        assertEquals("Beat It", song.title)
        assertEquals(listOf("Michael Jackson"), song.artists.map { it.name })
    }

    @Test
    fun `the normal home leaves song rows out`() {
        assertNull(HomePage.Section.fromMusicCarouselShelfRenderer(quickPicks))
    }

    @Test
    fun `a chip's podcast episodes play like video songs under their show`() {
        val section = HomePage.Section.fromMusicCarouselShelfRenderer(episodes, chipPage = true)

        val songs = section?.items?.map { it as SongItem }.orEmpty()
        assertEquals(listOf("e1", "e2"), songs.map { it.id })
        assertEquals(listOf("Episode one", "Episode two"), songs.map { it.title })
        assertEquals(listOf("Daily Talk"), songs.first().artists.map { it.name })
        assertNull(
            songs
                .first()
                .artists
                .single()
                .id,
        )
        assertEquals("MUSIC_VIDEO_TYPE_PODCAST_EPISODE", songs.first().musicVideoType)
        assertTrue(songs.all { it.isVideoSong })
        assertEquals("https://t/e1", songs.first().thumbnail)
    }

    @Test
    fun `a chip's podcast shows open as playlists`() {
        val section = HomePage.Section.fromMusicCarouselShelfRenderer(shows, chipPage = true)

        val playlists = section?.items?.map { it as PlaylistItem }.orEmpty()
        assertEquals(listOf("PLdaily", "PLweekly"), playlists.map { it.id })
        assertEquals("Host", playlists.first().author?.name)
        assertEquals("PLdaily", playlists.first().playEndpoint?.playlistId)
        assertNull(playlists.last().author)
    }

    @Test
    fun `the normal home leaves podcasts out`() {
        assertNull(HomePage.Section.fromMusicCarouselShelfRenderer(episodes))
        assertNull(HomePage.Section.fromMusicCarouselShelfRenderer(shows))
    }
}
