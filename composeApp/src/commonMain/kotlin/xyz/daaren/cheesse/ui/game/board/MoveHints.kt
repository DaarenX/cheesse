package xyz.daaren.cheesse.ui.game.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.ui.theme.CheesseTheme

/** Dots for quiet moves and rings for captures; they pop in when a piece is picked up. */
@Composable
fun MoveHints(
    moves: List<ChessMove>,
    boardRotation: PlayerColor,
    modifier: Modifier = Modifier,
) {
    val color = CheesseTheme.boardColors.moveHint
    val appear = remember(moves) { Animatable(0f) }
    LaunchedEffect(moves) {
        appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Canvas(modifier) {
        val squarePx = size.width / 8
        val progress = appear.value
        moves.distinctBy { it.to }.forEach { move ->
            val center = squareOffset(move.to, boardRotation, squarePx) + Offset(squarePx / 2, squarePx / 2)
            if (move.isCapture) {
                val stroke = squarePx * 0.08f
                val radius = (squarePx / 2 - stroke / 2) * (0.85f + 0.15f * progress)
                drawCircle(color, radius, center, alpha = progress.coerceIn(0f, 1f), style = Stroke(stroke))
            } else {
                drawCircle(color, squarePx * 0.17f * progress, center)
            }
        }
    }
}
