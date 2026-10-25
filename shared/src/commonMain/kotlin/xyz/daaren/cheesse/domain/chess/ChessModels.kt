package xyz.daaren.cheesse.domain.chess

import xyz.daaren.cheesse.DEFAULT_FEN
import xyz.daaren.cheesse.api.PlayerColor

enum class ChessPieceType {
    PAWN,
    KNIGHT,
    BISHOP,
    ROOK,
    QUEEN,
    KING,
}

data class ChessPiece(
    val type: ChessPieceType,
    val color: PlayerColor,
)

data class ChessSquare(
    val file: Char,
    val rank: Int,
) {
    val name: String = "$file$rank"
    val colIndex: Int = file - 'a'
    val rowIndex: Int = rank - 1

    override fun toString(): String = name

    companion object {
        fun fromString(name: String): ChessSquare {
            require(name.length == 2 && name[0] in 'a'..'h' && name[1] in '1'..'8') {
                "Invalid square notation: $name"
            }
            return ChessSquare(file = name[0], rank = name[1].digitToInt())
        }

        fun fromDisplay(
            displayCol: Int,
            displayRow: Int,
            playerColor: PlayerColor,
        ): ChessSquare {
            val col = if (playerColor == PlayerColor.WHITE) displayCol else 7 - displayCol
            val row = if (playerColor == PlayerColor.WHITE) 7 - displayRow else displayRow
            val file = 'a' + col.coerceIn(0, 7)
            val rank = row.coerceIn(0, 7) + 1
            return ChessSquare(file = file, rank = rank)
        }
    }
}

data class ChessMove(
    val from: ChessSquare,
    val to: ChessSquare,
    val uci: String,
    val isCapture: Boolean = false,
)

sealed interface ChessGameResult {
    data object Ongoing : ChessGameResult

    data class Win(
        val winner: PlayerColor,
    ) : ChessGameResult

    sealed interface Draw : ChessGameResult {
        data object Stalemate : Draw

        data object ThreefoldRepetition : Draw

        data object FiftyMoveRule : Draw

        data object InsufficientMaterial : Draw

        data object Other : Draw
    }

    val isOver: Boolean
        get() = this !is Ongoing
}

data class ChessBoardState(
    val piecePositions: List<Pair<ChessPiece, ChessSquare>>,
    val legalMoves: List<ChessMove>,
    val turn: PlayerColor,
    val capturedPieces: List<ChessPiece>,
    val gameResult: ChessGameResult = ChessGameResult.Ongoing,
)

/**
 * A game as its start position plus every move played since. Draw rules such as threefold
 * repetition depend on earlier positions, so the engine always replays the full history.
 */
data class ChessGameHistory(
    val startFen: String = DEFAULT_FEN,
    val movesUci: List<String> = emptyList(),
) {
    fun withMove(moveUci: String): ChessGameHistory = copy(movesUci = movesUci + moveUci)
}

sealed interface ChessMoveResult {
    data class Success(
        val history: ChessGameHistory,
        val fen: String,
        val pgn: String,
    ) : ChessMoveResult

    data class IllegalMove(
        val moveUci: String,
    ) : ChessMoveResult

    data class GameOver(
        val gameResult: ChessGameResult,
    ) : ChessMoveResult
}
