package xyz.daaren.cheesse.ui.game

import cafe.adriel.voyager.core.model.screenModelScope
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.data.GameSession
import xyz.daaren.cheesse.domain.chess.ChessEngineAdapter
import xyz.daaren.cheesse.domain.chess.ChessGameHistory
import xyz.daaren.cheesse.domain.chess.ChessMoveResult
import xyz.daaren.cheesse.domain.chess.DefaultChessEngineAdapter

class GameScreenModel(
    private val gameSession: GameSession,
    private val chessEngineAdapter: ChessEngineAdapter = DefaultChessEngineAdapter(),
) : GameScreenModelApi {
    private var currentHistory = ChessGameHistory()

    override val gameState = MutableStateFlow(buildGameState(currentHistory))

    init {
        screenModelScope.launch {
            gameSession.updates.collect { history ->
                currentHistory = history
                gameState.emit(buildGameState(history))
            }
        }
    }

    override fun makeMove(moveUci: String) {
        val result = chessEngineAdapter.applyMove(currentHistory, moveUci)
        if (result is ChessMoveResult.Success) {
            currentHistory = result.history
            gameState.value = buildGameState(result.history)
        } else {
            logger.warn("Failed to make move $moveUci: $result")
        }
        screenModelScope.launch {
            gameSession.sendMove(moveUci)
        }
    }

    private fun buildGameState(history: ChessGameHistory): GameScreenModelApi.GameState {
        val boardState = chessEngineAdapter.getBoardState(history)
        return GameScreenModelApi.GameState(
            piecePositions = boardState.piecePositions,
            legalMoves =
                if (gameSession.isTurnAllowed(boardState.turn)) {
                    boardState.legalMoves
                } else {
                    emptyList()
                },
            turn = boardState.turn,
            capturedPieces = boardState.capturedPieces,
            gameResult = boardState.gameResult,
        )
    }
}

private val logger = KtorSimpleLogger("xyz.daaren.cheesse.ui.game.GameScreenModel")
