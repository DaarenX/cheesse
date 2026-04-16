package xyz.daaren.cheesse.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateGameRequest(
    val color: GameColorPreference,
)

@Serializable
enum class GameColorPreference {
    WHITE,
    BLACK,
    RANDOM,
}

@Serializable
data class JoinGameRequest(
    val token: String,
)
