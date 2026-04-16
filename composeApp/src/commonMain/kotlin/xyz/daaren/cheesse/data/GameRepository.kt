package xyz.daaren.cheesse.data

import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.CreateGameRequest
import xyz.daaren.cheesse.api.CreateGameResponse
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.JoinGameRequest
import xyz.daaren.cheesse.api.JoinGameResponse
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.network.createHttpClient

interface GameRepository {
    suspend fun getGameConnection(
        id: Long,
        playerToken: String,
    ): ChessConnection

    suspend fun createGame(color: GameColorPreference): CreateGameResponse

    suspend fun joinGame(token: String): JoinGameResponse
}

class GameRepositoryImpl : GameRepository {
    val httpClient = createHttpClient()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun getGameConnection(
        id: Long,
        playerToken: String,
    ): ChessConnection {
        val outgoing = Channel<ClientMessage>(Channel.UNLIMITED)
        val updates = MutableSharedFlow<ServerMessage>(replay = 1, extraBufferCapacity = 16)

        repositoryScope.launch {
            try {
                httpClient.webSocket("/ws/game-session/$id?playerToken=$playerToken") {
                    launch {
                        for (message in outgoing) {
                            sendSerialized(message)
                        }
                    }

                    while (true) {
                        updates.emit(receiveDeserialized())
                    }
                }
            } finally {
                try {
                    outgoing.close()
                } catch (_: Exception) {
                }
            }
        }

        return ChessConnection(
            updates = updates.asSharedFlow(),
            sendMove = { outgoing.send(it) },
        )
    }

    override suspend fun createGame(color: GameColorPreference): CreateGameResponse =
        httpClient
            .post("/games") {
                contentType(ContentType.Application.Json)
                setBody(CreateGameRequest(color))
            }.body()

    override suspend fun joinGame(token: String): JoinGameResponse =
        httpClient
            .post("/games/join") {
                contentType(ContentType.Application.Json)
                setBody(JoinGameRequest(token))
            }.body()
}
