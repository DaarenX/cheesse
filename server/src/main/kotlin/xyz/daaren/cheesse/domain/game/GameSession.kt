package xyz.daaren.cheesse.domain.game

import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessGameResult
import java.util.UUID

data class CreatedGame(
    val gameId: Long,
    val joinToken: UUID,
    val playerId: Long,
    val playerToken: UUID,
    val color: PlayerColor,
)

data class JoinedGame(
    val gameId: Long,
    val playerId: Long,
    val playerToken: UUID,
    val color: PlayerColor,
)

data class AuthenticatedParticipant(
    val gameId: Long,
    val playerId: Long,
    val color: PlayerColor,
)

sealed interface MoveExecutionResult {
    data class Success(
        val game: Game,
    ) : MoveExecutionResult

    data object NotYourTurn : MoveExecutionResult

    data class IllegalMove(
        val moveUci: String,
    ) : MoveExecutionResult

    data class GameOver(
        val gameResult: ChessGameResult,
    ) : MoveExecutionResult

    data object GameNotFound : MoveExecutionResult
}
