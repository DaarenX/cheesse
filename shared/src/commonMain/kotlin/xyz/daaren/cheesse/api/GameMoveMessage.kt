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
    /**
     * [startFen] and [movesUci] carry the full game history so clients can detect repetition draws.
     * Both are optional so clients stay compatible with servers that only send [fen].
     */
    @Serializable
    data class Move(
        val fen: String,
        val startFen: String? = null,
        val movesUci: List<String> = emptyList(),
    ) : ServerMessage()

    @Serializable
    data class Error(
        val message: String,
    ) : ServerMessage()

    @Serializable
    data object GameStart : ServerMessage()
}
