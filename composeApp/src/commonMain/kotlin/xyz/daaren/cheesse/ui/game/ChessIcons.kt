package xyz.daaren.cheesse.ui.game

import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.chess_bishop_2
import cheesse.composeapp.generated.resources.chess_king_2
import cheesse.composeapp.generated.resources.chess_knight
import cheesse.composeapp.generated.resources.chess_pawn
import cheesse.composeapp.generated.resources.chess_queen
import cheesse.composeapp.generated.resources.chess_rook
import org.jetbrains.compose.resources.DrawableResource
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessPieceType

fun ChessPieceType.getIcon(): DrawableResource =
    when (this) {
        ChessPieceType.PAWN -> Res.drawable.chess_pawn
        ChessPieceType.KNIGHT -> Res.drawable.chess_knight
        ChessPieceType.BISHOP -> Res.drawable.chess_bishop_2
        ChessPieceType.ROOK -> Res.drawable.chess_rook
        ChessPieceType.QUEEN -> Res.drawable.chess_queen
        ChessPieceType.KING -> Res.drawable.chess_king_2
    }

fun ChessPiece.getIcon(): DrawableResource = type.getIcon()
