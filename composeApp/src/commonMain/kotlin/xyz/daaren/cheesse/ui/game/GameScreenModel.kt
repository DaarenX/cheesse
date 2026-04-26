package xyz.daaren.cheesse.ui.game

import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alluhemanth.chess.core.ChessGame
import io.github.alluhemanth.chess.core.piece.Piece
import io.github.alluhemanth.chess.core.piece.PieceColor
import io.github.alluhemanth.chess.core.piece.PieceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.data.GameSession
import xyz.daaren.cheesse.data.OfflineGameSession
import xyz.daaren.cheesse.makeUciMoveWorkaround

class GameScreenModel(
    private val gameSession: GameSession,
) : GameScreenModelApi {
    private val internalGame = ChessGame()

    override val gameState = MutableStateFlow(internalGame.toGameState())

    init {
        screenModelScope.launch {
            gameSession.updates.collect {

                internalGame.loadFen(it)
                gameState.emit(internalGame.toGameState())
            }
        }
    }

    override fun makeMove(moveUci: String) {
        internalGame.makeUciMoveWorkaround(moveUci)
        screenModelScope.launch {
            gameSession.sendMove(moveUci)
            gameState.emit(internalGame.toGameState())
        }
    }

    // TODO remove manual check for OfflineGameSession
    private fun ChessGame.toGameState(): GameScreenModelApi.GameState {
        val piecesOnBoard = getBoard().getAllPieces().map { it.first }
        return GameScreenModelApi.GameState(
            piecePositions = getBoard().getAllPieces(),
            legalMoves =
                if (getCurrentPlayer().toPlayerColor() == gameSession.playerColor ||
                    gameSession is OfflineGameSession
                ) {
                    getLegalMoves()
                } else {
                    emptyList()
                },
            turn = getCurrentPlayer().toPlayerColor(),
            capturedPieces = calculateCapturedPieces(piecesOnBoard),
            gameResult = getGameResult(),
        )
    }

    private fun calculateCapturedPieces(piecesOnBoard: List<Piece>): List<Piece> {
        val whiteOnBoard = piecesOnBoard.filter { it.color == PieceColor.WHITE }
        val blackOnBoard = piecesOnBoard.filter { it.color == PieceColor.BLACK }

        return calculateCapturedForColor(whiteOnBoard, PieceColor.WHITE) +
            calculateCapturedForColor(blackOnBoard, PieceColor.BLACK)
    }

    private fun calculateCapturedForColor(
        onBoard: List<Piece>,
        color: PieceColor,
    ): List<Piece> {
        val standard =
            listOf(
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.PAWN,
                PieceType.KNIGHT,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.BISHOP,
                PieceType.ROOK,
                PieceType.ROOK,
                PieceType.QUEEN,
            )

        val onBoardTypes = onBoard.map { it.pieceType }.toMutableList()
        val captured = mutableListOf<Piece>()

        standard.forEach { type ->
            if (onBoardTypes.contains(type)) {
                onBoardTypes.remove(type)
            } else {
                captured.add(Piece(type, color))
            }
        }
        return captured
    }

    private fun PieceColor.toPlayerColor(): PlayerColor =
        when (this) {
            PieceColor.WHITE -> PlayerColor.WHITE
            PieceColor.BLACK -> PlayerColor.BLACK
        }
}
