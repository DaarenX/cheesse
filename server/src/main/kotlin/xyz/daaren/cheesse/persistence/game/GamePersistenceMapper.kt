package xyz.daaren.cheesse.persistence.game

import xyz.daaren.cheesse.domain.chess.ChessGameHistory
import xyz.daaren.cheesse.domain.game.Game
import xyz.daaren.cheesse.domain.game.PlayerSeat
import java.util.UUID

fun GameEntity.toDomainModel(): Game {
    val gameId = id ?: error("Game entity is missing id")

    return Game(
        id = gameId,
        fen = fen,
        pgn = pgn,
        history = ChessGameHistory(startFen, moveHistory.split(' ').filter { it.isNotEmpty() }),
        joinToken = joinToken,
        whiteSeat = toPlayerSeat(gameId, "white", whitePlayerId, whitePlayerToken),
        blackSeat = toPlayerSeat(gameId, "black", blackPlayerId, blackPlayerToken),
    )
}

fun GameEntity.withHistory(
    history: ChessGameHistory,
    fen: String,
    pgn: String,
): GameEntity =
    copy(
        fen = fen,
        pgn = pgn,
        startFen = history.startFen,
        moveHistory = history.movesUci.joinToString(" "),
    )

private fun toPlayerSeat(
    gameId: Long,
    color: String,
    playerId: Long?,
    playerToken: UUID?,
): PlayerSeat? {
    if (playerId == null && playerToken == null) {
        return null
    }

    require(playerId != null && playerToken != null) {
        "Game $gameId has incomplete $color player data"
    }

    return PlayerSeat(
        playerId = playerId,
        playerToken = playerToken,
    )
}
