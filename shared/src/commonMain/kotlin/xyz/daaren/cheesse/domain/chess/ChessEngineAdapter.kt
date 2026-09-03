package xyz.daaren.cheesse.domain.chess

import xyz.daaren.cheesse.api.PlayerColor

interface ChessEngineAdapter {
    fun activePlayerColor(fen: String): PlayerColor

    fun applyMove(
        history: ChessGameHistory,
        moveUci: String,
    ): ChessMoveResult

    fun getBoardState(history: ChessGameHistory): ChessBoardState
}
