package com.arubr.smsvcodes.ui.components.musicplayer.mini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.arubr.smsvcodes.data.local.CoverShape
import com.arubr.smsvcodes.data.local.DefaultCoverShapes
import com.arubr.smsvcodes.data.local.DefaultMiniBarButtons
import com.arubr.smsvcodes.data.local.MiniBarBackground
import com.arubr.smsvcodes.data.local.MiniBarButton
import com.arubr.smsvcodes.data.local.MiniBarProgress
import com.arubr.smsvcodes.data.local.MiniCoverMotion
import com.arubr.smsvcodes.data.local.MiniCoverShape
import com.arubr.smsvcodes.data.local.MiniCoverTap
import com.arubr.smsvcodes.data.local.MiniLongPress
import com.arubr.smsvcodes.data.local.PlayerPreferences

/** How the music mini player looks, as chosen in Appearance › Player. */
@Immutable
internal data class MiniPlayerLook(
    val coverShape: MiniCoverShape = MiniCoverShape.CIRCLE,
    val coverMotion: MiniCoverMotion = MiniCoverMotion.ON_PLAY,
    val coverShapes: List<CoverShape> = DefaultCoverShapes,
    val pausedShape: CoverShape? = CoverShape.CIRCLE,
    val barProgress: MiniBarProgress = MiniBarProgress.NONE,
    val coverRing: Boolean = true,
    val buttons: List<MiniBarButton> = DefaultMiniBarButtons,
    val coverTap: MiniCoverTap = MiniCoverTap.OPEN,
    val longPress: MiniLongPress = MiniLongPress.NOTHING,
    val background: MiniBarBackground = MiniBarBackground.TINTED,
) {
    companion object {
        /** Today's bar, for hosts with no choices of their own, such as TV. */
        val Plain = MiniPlayerLook()
    }
}

/** The user's mini player look, read once per host. */
@Composable
internal fun rememberMiniPlayerLook(): MiniPlayerLook {
    val context = LocalContext.current
    val preferences = remember(context) { PlayerPreferences(context) }
    val coverShape by preferences.miniCoverShape.collectAsState(initial = MiniCoverShape.MORPHING)
    val coverMotion by preferences.miniCoverMotion.collectAsState(initial = MiniCoverMotion.ON_PLAY)
    val coverShapes by preferences.miniCoverShapes.collectAsState(initial = DefaultCoverShapes)
    val pausedShape by preferences.miniCoverPausedShape.collectAsState(initial = CoverShape.CIRCLE)
    val barProgress by preferences.miniBarProgress.collectAsState(initial = MiniBarProgress.NONE)
    val coverRing by preferences.miniCoverRing.collectAsState(initial = true)
    val buttons by preferences.miniBarButtons.collectAsState(initial = DefaultMiniBarButtons)
    val coverTap by preferences.miniCoverTap.collectAsState(initial = MiniCoverTap.OPEN)
    val longPress by preferences.miniLongPress.collectAsState(initial = MiniLongPress.NOTHING)
    val background by preferences.miniBarBackground.collectAsState(initial = MiniBarBackground.TINTED)
    return remember(coverShape, coverMotion, coverShapes, pausedShape, barProgress, coverRing, buttons, coverTap, longPress, background) {
        MiniPlayerLook(coverShape, coverMotion, coverShapes, pausedShape, barProgress, coverRing, buttons, coverTap, longPress, background)
    }
}
