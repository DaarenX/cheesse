package xyz.daaren.cheesse.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessPieceType
import xyz.daaren.cheesse.ui.components.PulsingDot
import xyz.daaren.cheesse.ui.game.board.PieceGlyph
import xyz.daaren.cheesse.ui.theme.CheesseTheme
import xyz.daaren.cheesse.ui.theme.Motion
import xyz.daaren.cheesse.ui.theme.PillShape

data class PlayerBarState(
    val color: PlayerColor,
    val isTurn: Boolean,
    val isYou: Boolean,
    val captured: List<ChessPiece>,
    val lead: Int,
)

fun playerBarState(
    color: PlayerColor,
    gameState: GameScreenModelApi.GameState,
    youColor: PlayerColor?,
) = PlayerBarState(
    color = color,
    isTurn = gameState.turn == color && !gameState.gameResult.isOver,
    isYou = youColor == color,
    captured = capturedBy(color, gameState.capturedPieces),
    lead = materialLead(color, gameState.capturedPieces),
)

/** One side of the game: who it is, what they captured, and whether it's their move. */
@Composable
fun PlayerBar(
    state: PlayerBarState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(if (state.isTurn) colors.primaryContainer else colors.surfaceContainerLow, Motion.colorChange)
    val border by animateColorAsState(if (state.isTurn) colors.primary else colors.outlineVariant, Motion.colorChange)
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.5.dp, border, shape)
            .padding(12.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(state.color)
        Column(Modifier.weight(1f)) {
            NameRow(state)
            CapturedStrip(state.captured, state.lead)
        }
        AnimatedVisibility(state.isTurn, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            TurnBadge()
        }
    }
}

@Composable
private fun Avatar(color: PlayerColor) {
    val board = CheesseTheme.boardColors
    Box(
        Modifier.size(40.dp).background(if (color == PlayerColor.WHITE) board.darkSquare else board.lightSquare, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        PieceGlyph(ChessPiece(ChessPieceType.KING, color), Modifier.size(28.dp))
    }
}

@Composable
private fun NameRow(state: PlayerBarState) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(state.color.displayName(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if (state.isYou) {
            Text(
                "You",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, PillShape).padding(8.dp, 2.dp),
            )
        }
    }
}

@Composable
private fun CapturedStrip(
    captured: List<ChessPiece>,
    lead: Int,
) {
    Row(Modifier.height(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
            captured.forEach { PieceGlyph(it, Modifier.size(18.dp)) }
        }
        if (lead > 0) {
            Text(
                "+$lead",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun TurnBadge() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        PulsingDot(MaterialTheme.colorScheme.primary)
        Text("to move", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
