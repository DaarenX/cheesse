package xyz.daaren.cheesse.domain.chess

import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.chess.core.ChessGame
import xyz.daaren.chess.core.exception.ChessCoreException

class DefaultChessEngineAdapter : ChessEngineAdapter {
    override fun activePlayerColor(fen: String): PlayerColor {
        val parts = fen.split(" ")
        return if (parts.getOrNull(1) == "b") PlayerColor.BLACK else PlayerColor.WHITE
    }

    override fun applyMove(
        history: ChessGameHistory,
        moveUci: String,
    ): ChessMoveResult {
        val chessGame = replay(history)
        val gameResult = chessGame.getGameResult().toDomain()
        return when {
            gameResult.isOver -> ChessMoveResult.GameOver(gameResult)
            !chessGame.tryUciMove(moveUci) -> ChessMoveResult.IllegalMove(moveUci)
            else -> ChessMoveResult.Success(history.withMove(moveUci), chessGame.getFen(), chessGame.getPgn())
        }
    }

    override fun getBoardState(history: ChessGameHistory): ChessBoardState {
        val chessGame = replay(history)
        val pieces =
            chessGame.getBoard().getAllPieces().map { (piece, square) ->
                piece.toDomain() to square.toDomain()
            }
        val gameResult = chessGame.getGameResult().toDomain()
        val legalMoves = if (gameResult.isOver) emptyList() else chessGame.getLegalMoves().map { it.toDomain() }

        return ChessBoardState(
            piecePositions = pieces,
            legalMoves = legalMoves,
            turn = chessGame.getCurrentPlayer().toPlayerColor(),
            capturedPieces = capturedPieces(pieces.map { it.first }),
            gameResult = gameResult,
        )
    }

    // Loading only the latest FEN would drop the position history that repetition detection needs.
    private fun replay(history: ChessGameHistory): ChessGame =
        ChessGame().apply {
            loadFen(history.startFen)
            history.movesUci.forEach { moveUci ->
                require(tryUciMove(moveUci)) { "Game history contains illegal move $moveUci" }
            }
        }

    private fun ChessGame.tryUciMove(moveUci: String): Boolean =
        try {
            moveUci.length in 4..5 && makeUciMove(moveUci)
        } catch (_: ChessCoreException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
}
