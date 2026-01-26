package xyz.daaren.cheesse.api.game.dto

import xyz.daaren.cheesse.domain.game.GameColorPreference

data class CreateGameRequest(
    val color: GameColorPreference,
)

data class JoinGameRequest(
    val token: String,
)
