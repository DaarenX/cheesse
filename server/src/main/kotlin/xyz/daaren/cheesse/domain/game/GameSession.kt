package xyz.daaren.cheesse.domain.game

import xyz.daaren.cheesse.api.PlayerColor

data class CreatedGame(
    val gameId: Long,
    val joinToken: String,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)

data class JoinedGame(
    val gameId: Long,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)
