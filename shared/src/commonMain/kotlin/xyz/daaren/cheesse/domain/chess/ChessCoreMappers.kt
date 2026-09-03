package xyz.daaren.cheesse.domain.chess

import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.chess.core.board.Square
import xyz.daaren.chess.core.game.GameResult
import xyz.daaren.chess.core.move.Move
import xyz.daaren.chess.core.piece.Piece
import xyz.daaren.chess.core.piece.PieceColor
import xyz.daaren.chess.core.piece.PieceType

internal fun PieceColor.toPlayerColor(): PlayerColor =
    when (this) {
        PieceColor.WHITE -> PlayerColor.WHITE
        PieceColor.BLACK -> PlayerColor.BLACK
    }

internal fun PieceType.toDomain(): ChessPieceType =
    when (this) {
        PieceType.PAWN -> ChessPieceType.PAWN
        PieceType.KNIGHT -> ChessPieceType.KNIGHT
        PieceType.BISHOP -> ChessPieceType.BISHOP
        PieceType.ROOK -> ChessPieceType.ROOK
        PieceType.QUEEN -> ChessPieceType.QUEEN
        PieceType.KING -> ChessPieceType.KING
    }

internal fun Piece.toDomain(): ChessPiece =
    ChessPiece(
        type = pieceType.toDomain(),
        color = color.toPlayerColor(),
    )

internal fun Square.toDomain(): ChessSquare =
    ChessSquare(
        file = file.value,
        rank = rank.value,
    )

internal fun Move.toDomain(): ChessMove =
    ChessMove(
        from = from.toDomain(),
        to = to.toDomain(),
        uci = toUci(),
        isCapture = isCapture,
    )

internal fun GameResult.toDomain(): ChessGameResult =
    when (this) {
        is GameResult.Win -> ChessGameResult.Win(winner = winner.toPlayerColor())
        is GameResult.Draw.Stalemate -> ChessGameResult.Draw.Stalemate
        is GameResult.Draw.ThreefoldRepetition -> ChessGameResult.Draw.ThreefoldRepetition
        is GameResult.Draw.FiftyMoveRule -> ChessGameResult.Draw.FiftyMoveRule
        is GameResult.Draw.InsufficientMaterial -> ChessGameResult.Draw.InsufficientMaterial
        is GameResult.Draw -> ChessGameResult.Draw.Other
        is GameResult.Ongoing -> ChessGameResult.Ongoing
    }
