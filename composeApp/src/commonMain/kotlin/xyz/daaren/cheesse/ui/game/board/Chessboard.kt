package xyz.daaren.cheesse.ui.game.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessSquare
import xyz.daaren.cheesse.ui.game.GameScreenModelApi.GameState
import xyz.daaren.cheesse.ui.game.PromotionDialog
import xyz.daaren.cheesse.ui.game.PromotionRequest
import xyz.daaren.cheesse.ui.theme.CheesseTheme

/** The interactive board: squares, highlights, animated pieces, move hints and the promotion picker. */
@Composable
fun Chessboard(
    gameState: GameState,
    boardRotation: PlayerColor,
    onMove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val board = remember { BoardInteractionState() }
    board.pendingPromotion?.let { base ->
        PromotionDialog(
            request = PromotionRequest(base, gameState.turn),
            onSelect = { uci ->
                board.pendingPromotion = null
                onMove(uci)
            },
            onDismiss = { board.pendingPromotion = null },
        )
    }
    BoardFrame(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val squareSize = maxWidth / 8
            val layout = BoardLayout(squareSize, with(LocalDensity.current) { squareSize.toPx() }, boardRotation)
            val highlights = BoardHighlights(gameState.lastMove, board.selected, gameState.checkedKingSquare)
            BoardBackground(highlights, boardRotation, Modifier.fillMaxSize())
            BoardCoordinates(boardRotation, Modifier.fillMaxSize())
            val handler = board.dragHandler(gameState, layout, onMove)
            piecePlacements(gameState, layout, board.localDrop).forEach { placement ->
                key(placement.square) { BoardPiece(placement, layout, handler) }
            }
            MoveHints(gameState.legalMoves.filter { it.from == board.selected }, boardRotation, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun BoardFrame(
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val outer = RoundedCornerShape(14.dp)
    Box(
        modifier
            .aspectRatio(1f)
            .shadow(10.dp, outer)
            .background(CheesseTheme.boardColors.frame, outer)
            .padding(6.dp)
            .clip(RoundedCornerShape(8.dp)),
        content = content,
    )
}

/** UI-only state of the board that does not belong in the ScreenModel. */
private class BoardInteractionState {
    var selected by mutableStateOf<ChessSquare?>(null)
    var pendingPromotion by mutableStateOf<String?>(null)
    var localDrop by mutableStateOf<LocalDrop?>(null)

    fun dragHandler(
        gameState: GameState,
        layout: BoardLayout,
        onMove: (String) -> Unit,
    ) = PieceDragHandler(
        canDrag = { square -> gameState.legalMoves.any { it.from == square } },
        onPickUp = { selected = it },
        onRelease = { from, drag ->
            selected = null
            drag != null && handleDrop(DroppedPiece(from, drag), gameState, layout, onMove)
        },
    )

    private fun handleDrop(
        drop: DroppedPiece,
        gameState: GameState,
        layout: BoardLayout,
        onMove: (String) -> Unit,
    ): Boolean {
        val target = drop.targetSquare(layout) ?: return false
        return when (val decision = resolveDrop(gameState.legalMoves, drop.from, target)) {
            is DropDecision.Play -> {
                localDrop = LocalDrop(decision.uci, drop.residual(target, layout))
                onMove(decision.uci)
                true
            }
            is DropDecision.Promote -> {
                pendingPromotion = decision.baseUci
                false
            }
            DropDecision.Illegal -> false
        }
    }
}
