package xyz.daaren.cheesse.ui.game

import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.chess_bishop_2
import cheesse.composeapp.generated.resources.chess_king_2
import cheesse.composeapp.generated.resources.chess_knight
import cheesse.composeapp.generated.resources.chess_pawn
import cheesse.composeapp.generated.resources.chess_queen
import cheesse.composeapp.generated.resources.chess_rook
import io.github.alluhemanth.chess.core.piece.Piece
import io.github.alluhemanth.chess.core.piece.PieceType
import org.jetbrains.compose.resources.DrawableResource

fun PieceType.getIcon(): DrawableResource =
    when (this) {
        PieceType.PAWN -> Res.drawable.chess_pawn
        PieceType.KNIGHT -> Res.drawable.chess_knight
        PieceType.BISHOP -> Res.drawable.chess_bishop_2
        PieceType.ROOK -> Res.drawable.chess_rook
        PieceType.QUEEN -> Res.drawable.chess_queen
        PieceType.KING -> Res.drawable.chess_king_2
    }

fun Piece.getIcon(): DrawableResource = pieceType.getIcon()
