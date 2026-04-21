package xyz.daaren.cheesse.data

import kotlinx.coroutines.flow.Flow
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage

data class GameSession(
    val id: Long,
    val playerToken: String,
    val playerColor: PlayerColor,
    val gameSessionConnection: GameSessionConnection,
)

class GameSessionConnection(
    val updates: Flow<ServerMessage>,
    val sendMove: suspend (ClientMessage.Move) -> Unit,
)
