package xyz.daaren.cheesse.persistence.game

import xyz.daaren.cheesse.domain.game.Game
import xyz.daaren.cheesse.domain.game.PlayerSeat

fun GameEntity.toDomainModel(): Game {
    val gameId = id ?: error("Game entity is missing id")

    return Game(
        id = gameId,
        fen = fen,
        pgn = pgn,
        joinToken = joinToken,
        whiteSeat = toPlayerSeat(gameId, "white", whitePlayerId, whitePlayerToken),
        blackSeat = toPlayerSeat(gameId, "black", blackPlayerId, blackPlayerToken),
    )
}

private fun toPlayerSeat(
    gameId: Long,
    color: String,
    playerId: Long?,
    playerToken: String?,
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
