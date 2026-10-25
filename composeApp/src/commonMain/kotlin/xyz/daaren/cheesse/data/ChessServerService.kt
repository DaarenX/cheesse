package xyz.daaren.cheesse.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.CreateGameRequest
import xyz.daaren.cheesse.api.CreateGameResponse
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.JoinGameRequest
import xyz.daaren.cheesse.api.JoinGameResponse
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.network.createHttpClient

interface ChessServerService {
    val serverUrl: StateFlow<String>

    fun updateBaseUrl(url: String)

    suspend fun connectToGameSession(
        id: Long,
        playerToken: String,
    ): GameSessionConnection

    suspend fun createGame(color: GameColorPreference): CreateGameResponse

    suspend fun joinGame(token: String): JoinGameResponse
}

class ChessServerServiceImpl : ChessServerService {
    private val _serverUrl = MutableStateFlow("http://127.0.0.1:8080")
    override val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private var httpClient: HttpClient = createHttpClient(_serverUrl.value)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun updateBaseUrl(url: String) {
        val normalized = normalizeBaseUrl(url)
        _serverUrl.value = normalized
        httpClient.close()
        httpClient = createHttpClient(normalized)
    }

    override suspend fun connectToGameSession(
        id: Long,
        playerToken: String,
    ): GameSessionConnection {
        val outgoing = Channel<ClientMessage>(Channel.UNLIMITED)
        val updates = MutableSharedFlow<ServerMessage>(replay = 1, extraBufferCapacity = 16)

        repositoryScope.launch {
            try {
                val currentUrl = normalizeBaseUrl(_serverUrl.value)
                val wsProtocol = if (currentUrl.startsWith("https")) "wss" else "ws"
                val baseUrlWithoutProtocol = currentUrl.substringAfter("://")
                val wsUrl = "$wsProtocol://$baseUrlWithoutProtocol/ws/game-session/$id?playerToken=$playerToken"

                httpClient.webSocket(wsUrl) {
                    launch {
                        for (message in outgoing) {
                            sendSerialized(message)
                        }
                    }

                    while (true) {
                        updates.emit(receiveDeserialized())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    outgoing.close()
                } catch (_: Exception) {
                }
            }
        }

        return GameSessionConnection(
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

    private fun normalizeBaseUrl(url: String): String = url.trim().trimEnd('/')
}
