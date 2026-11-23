package xyz.daaren.cheesse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Position (fractions of the screen) and radius (fraction of the shorter side) of one hole. */
private data class Hole(
    val x: Float,
    val y: Float,
    val radius: Float,
)

// A fixed layout so the pattern looks the same on every launch and platform.
private val holes =
    listOf(
        Hole(0.06f, 0.10f, 0.18f),
        Hole(0.94f, 0.22f, 0.11f),
        Hole(0.88f, 0.84f, 0.22f),
        Hole(0.12f, 0.88f, 0.13f),
        Hole(0.55f, 0.04f, 0.06f),
        Hole(0.30f, 0.62f, 0.04f),
        Hole(0.72f, 0.52f, 0.05f),
        Hole(0.42f, 0.95f, 0.07f),
    )

/** Soft, low-contrast circles that hint at the holes in a slice of cheese. */
@Composable
fun CheeseHolesBackground(modifier: Modifier = Modifier) {
    val first = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val second = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    Canvas(modifier.fillMaxSize()) {
        val unit = size.minDimension
        holes.forEachIndexed { index, hole ->
            val center = Offset(hole.x * size.width, hole.y * size.height)
            val radius = hole.radius * unit
            val color = if (index % 2 == 0) first else second
            val brush = Brush.radialGradient(listOf(color, color.copy(alpha = color.alpha * 0.4f)), center, radius)
            drawCircle(brush, radius, center)
            drawCircle(Color.Black.copy(alpha = 0.025f), radius, center.copy(y = center.y + radius * 0.06f))
        }
    }
}
