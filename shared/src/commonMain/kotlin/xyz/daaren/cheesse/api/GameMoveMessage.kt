package xyz.daaren.cheesse.api

import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage {
    @Serializable
    data class Move(
        val moveUci: String,
    ) : ClientMessage()
}

@Serializable
sealed class ServerMessage {
    @Serializable
    data class Move(
        val fen: String,
    ) : ServerMessage()

    @Serializable
    data class Error(
        val message: String,
    ) : ServerMessage()

    @Serializable
    data object GameStart : ServerMessage()
}
