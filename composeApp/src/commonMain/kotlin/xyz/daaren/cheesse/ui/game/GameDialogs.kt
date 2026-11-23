package xyz.daaren.cheesse.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.cheesse_icon
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessGameResult
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessPieceType
import xyz.daaren.cheesse.ui.components.ButtonOptions
import xyz.daaren.cheesse.ui.components.ButtonVariant
import xyz.daaren.cheesse.ui.components.CheesseButton
import xyz.daaren.cheesse.ui.components.CheesseDialog
import xyz.daaren.cheesse.ui.game.board.PieceGlyph
import xyz.daaren.cheesse.ui.theme.CheesseTheme

data class PromotionRequest(
    val baseUci: String,
    val color: PlayerColor,
)

private val promotionPieces = listOf(ChessPieceType.QUEEN, ChessPieceType.ROOK, ChessPieceType.BISHOP, ChessPieceType.KNIGHT)

@Composable
fun PromotionDialog(
    request: PromotionRequest,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    CheesseDialog(title = "Promote pawn", onDismissRequest = onDismiss) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            promotionPieces.forEach { type ->
                PromotionTile(ChessPiece(type, request.color)) { onSelect(request.baseUci + type.promotionSuffix()) }
            }
        }
    }
}

@Composable
private fun PromotionTile(
    piece: ChessPiece,
    onClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val board = CheesseTheme.boardColors
    Box(
        Modifier
            .size(64.dp)
            .clip(shape)
            .background(board.lightSquare)
            .border(1.5.dp, board.frame.copy(alpha = 0.5f), shape)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        PieceGlyph(piece, Modifier.fillMaxSize())
    }
}

private fun ChessPieceType.promotionSuffix(): String =
    when (this) {
        ChessPieceType.KNIGHT -> "n"
        ChessPieceType.BISHOP -> "b"
        ChessPieceType.ROOK -> "r"
        ChessPieceType.QUEEN -> "q"
        ChessPieceType.PAWN, ChessPieceType.KING -> ""
    }

@Composable
fun GameOverDialog(
    gameResult: ChessGameResult,
    onBackToMenu: () -> Unit,
    onDismiss: () -> Unit,
) {
    CheesseDialog(title = gameResult.title(), onDismissRequest = onDismiss) {
        Box(
            Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Res.drawable.cheesse_icon), contentDescription = null, modifier = Modifier.size(56.dp))
        }
        Text(
            gameResult.reason(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CheesseButton("Back to menu", onBackToMenu, Modifier.fillMaxWidth())
        CheesseButton("View board", onDismiss, Modifier.fillMaxWidth(), ButtonOptions(ButtonVariant.Text))
    }
}

private fun ChessGameResult.title(): String =
    when (this) {
        is ChessGameResult.Win -> "${winner.displayName()} wins!"
        is ChessGameResult.Draw -> "Draw"
        ChessGameResult.Ongoing -> "Game over"
    }

private fun ChessGameResult.reason(): String =
    when (this) {
        is ChessGameResult.Win -> "by checkmate"
        ChessGameResult.Draw.Stalemate -> "by stalemate"
        ChessGameResult.Draw.ThreefoldRepetition -> "by threefold repetition"
        ChessGameResult.Draw.FiftyMoveRule -> "by the fifty-move rule"
        ChessGameResult.Draw.InsufficientMaterial -> "by insufficient material"
        ChessGameResult.Draw.Other, ChessGameResult.Ongoing -> ""
    }

fun PlayerColor.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
