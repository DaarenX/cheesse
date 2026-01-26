package xyz.daaren.cheesse.api.game.dto

import xyz.daaren.cheesse.domain.game.PlayerColor

data class GameResponse(
    val id: Long,
    val fen: String,
    val pgn: String,
    val whitePlayerId: Long?,
    val blackPlayerId: Long?,
)

data class CreateGameResponse(
    val gameId: Long,
    val joinToken: String,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)

data class JoinGameResponse(
    val gameId: Long,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)
