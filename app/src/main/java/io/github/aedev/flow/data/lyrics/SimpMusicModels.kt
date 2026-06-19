// ============================================================================
// THIS IMPLEMENTATION WAS INSPIRED BY METROLIST
// ============================================================================

package com.arubr.smsvcodes.data.lyrics

/**
 * Data classes for the SimpMusic API responses.
 */
data class SimpMusicLyricsData(
    val richSyncLyrics: String?,    
    val syncedLyrics: String?,      
    val plainLyrics: String?,       
    val duration: Double? = null
)

data class SimpMusicApiResponse(
    val lyrics: List<SimpMusicLyricsData>?
)
