package xyz.daaren.cheesse.api

import kotlinx.serialization.Serializable

@Serializable
data class GameResponse(
    val id: Long,
    val fen: String,
    val pgn: String,
    val whitePlayerId: Long?,
    val blackPlayerId: Long?,
)

@Serializable
data class CreateGameResponse(
    val gameId: Long,
    val joinToken: String,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)

@Serializable
data class JoinGameResponse(
    val gameId: Long,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)

@Serializable
enum class PlayerColor {
    WHITE,
    BLACK,
}
