package xyz.daaren.cheesse.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/** Shared animation specs so the whole app moves with the same rhythm. */
object Motion {
    const val SHORT_MS = 180
    const val MEDIUM_MS = 220
    const val LONG_MS = 320

    val pieceMove = tween<Offset>(MEDIUM_MS, easing = FastOutSlowInEasing)
    val snapBack = spring<Offset>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    val press = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val dialogEnter = tween<Float>(SHORT_MS, easing = FastOutSlowInEasing)
    val colorChange = tween<Color>(MEDIUM_MS, easing = FastOutSlowInEasing)
}
