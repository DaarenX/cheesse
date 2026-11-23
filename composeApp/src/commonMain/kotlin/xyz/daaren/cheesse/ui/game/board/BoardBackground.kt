package xyz.daaren.cheesse.ui.game.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.domain.chess.ChessSquare
import xyz.daaren.cheesse.ui.theme.BoardColors
import xyz.daaren.cheesse.ui.theme.CheesseTheme
import xyz.daaren.cheesse.ui.theme.Motion

/** Squares that should stand out on the board. */
data class BoardHighlights(
    val lastMove: ChessMove? = null,
    val selected: ChessSquare? = null,
    val checkedKing: ChessSquare? = null,
)

@Composable
fun BoardBackground(
    highlights: BoardHighlights,
    boardRotation: PlayerColor,
    modifier: Modifier = Modifier,
) {
    val colors = CheesseTheme.boardColors
    val lastMoveAlpha = remember(highlights.lastMove) { Animatable(0f) }
    LaunchedEffect(highlights.lastMove) { lastMoveAlpha.animateTo(1f, tween(Motion.MEDIUM_MS)) }
    Canvas(modifier) {
        val squarePx = size.width / 8
        drawSquares(colors, squarePx)
        highlights.lastMove?.let { move ->
            val tint = colors.lastMove.copy(alpha = colors.lastMove.alpha * lastMoveAlpha.value)
            listOf(move.from, move.to).forEach { fillSquare(squareOffset(it, boardRotation, squarePx), squarePx, tint) }
        }
        highlights.selected?.let { drawSelection(squareOffset(it, boardRotation, squarePx), squarePx, colors.selected) }
        highlights.checkedKing?.let { drawCheck(squareOffset(it, boardRotation, squarePx), squarePx, colors.check) }
    }
}

private fun DrawScope.drawSquares(
    colors: BoardColors,
    squarePx: Float,
) {
    drawRect(colors.lightSquare)
    for (row in 0 until 8) {
        for (col in 0 until 8) {
            if ((row + col) % 2 != 0) fillSquare(Offset(col * squarePx, row * squarePx), squarePx, colors.darkSquare)
        }
    }
}

private fun DrawScope.fillSquare(
    topLeft: Offset,
    squarePx: Float,
    color: Color,
) = drawRect(color, topLeft, Size(squarePx, squarePx))

private fun DrawScope.drawSelection(
    topLeft: Offset,
    squarePx: Float,
    color: Color,
) {
    fillSquare(topLeft, squarePx, color.copy(alpha = 0.25f))
    val inset = squarePx * 0.04f
    drawRect(
        color = color,
        topLeft = topLeft + Offset(inset, inset),
        size = Size(squarePx - 2 * inset, squarePx - 2 * inset),
        style = Stroke(width = squarePx * 0.06f),
    )
}

private fun DrawScope.drawCheck(
    topLeft: Offset,
    squarePx: Float,
    color: Color,
) {
    val center = topLeft + Offset(squarePx / 2, squarePx / 2)
    val brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0.55f), Color.Transparent), center, squarePx * 0.7f)
    drawRect(brush, topLeft, Size(squarePx, squarePx))
}
