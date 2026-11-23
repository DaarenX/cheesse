package xyz.daaren.cheesse.ui.game.board

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.ui.theme.CheesseTheme

/** File letters along the bottom edge and rank numbers along the right edge. */
@Composable
fun BoardCoordinates(
    boardRotation: PlayerColor,
    modifier: Modifier = Modifier,
) {
    val colors = CheesseTheme.boardColors
    val measurer = rememberTextMeasurer()
    val base = MaterialTheme.typography.labelSmall
    Canvas(modifier) {
        val squarePx = size.width / 8
        val padding = 3.dp.toPx()
        repeat(8) { index ->
            // Squares with an even index on the bottom row and right column are dark, so labels use the light color there.
            val onDark = index % 2 == 0
            val style = base.copy(color = if (onDark) colors.lightSquare else colors.darkSquare)
            val file = if (boardRotation == PlayerColor.WHITE) 'a' + index else 'h' - index
            val rank = if (boardRotation == PlayerColor.WHITE) '8' - index else '1' + index
            drawLabel(measurer, file.toString(), style) { Offset(index * squarePx + padding, size.height - it.y - padding) }
            drawLabel(measurer, rank.toString(), style) {
                Offset(size.width - it.x - padding, index * squarePx + padding)
            }
        }
    }
}

private fun DrawScope.drawLabel(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    position: (Offset) -> Offset,
) {
    val layout = measurer.measure(text, style)
    drawText(layout, topLeft = position(Offset(layout.size.width.toFloat(), layout.size.height.toFloat())))
}
