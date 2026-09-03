package xyz.daaren.cheesse.websocket.game

import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.domain.game.AuthenticatedParticipant
import xyz.daaren.cheesse.domain.game.Game
import xyz.daaren.cheesse.domain.game.GameService
import xyz.daaren.cheesse.domain.game.MoveExecutionResult
import java.util.concurrent.ConcurrentHashMap

@Component
class GameSessionWebSocketHandler(
    private val gameService: GameService,
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(GameSessionWebSocketHandler::class.java)
    private val sessionsByGameId = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    override fun handle(session: WebSocketSession): Mono<Void> =
        mono {
            authenticateSession(session)
        }.flatMap { participant ->
            val gameId = participant.gameId
            val sessionsForGame = sessionsByGameId.computeIfAbsent(gameId) { ConcurrentHashMap.newKeySet() }
            sessionsForGame.add(session)
            logger.info(
                "WebSocket connected gameId={} playerId={} color={} sessionId={} activeSessions={}",
                participant.gameId,
                participant.playerId,
                participant.color,
                session.id,
                sessionsForGame.count { it.isOpen },
            )

            notifyGameStartIfReady(gameId)
                .then(
                    session
                        .receive()
                        .concatMap { message ->
                            logger.info(
                                "WebSocket message received gameId={} playerId={} sessionId={} payload={}",
                                participant.gameId,
                                participant.playerId,
                                session.id,
                                message.payloadAsText,
                            )
                            processMove(message.payloadAsText, participant)
                        }.concatMap { action ->
                            when (action) {
                                is SessionAction.Broadcast -> broadcastToGame(gameId, action.payload)

                                is SessionAction.Reply -> {
                                    logger.info(
                                        "WebSocket reply gameId={} playerId={} sessionId={} payload={}",
                                        participant.gameId,
                                        participant.playerId,
                                        session.id,
                                        action.payload,
                                    )
                                    session.send(Mono.just(session.textMessage(action.payload))).then()
                                }

                                is SessionAction.Close -> {
                                    logger.info(
                                        "WebSocket closing gameId={} playerId={} sessionId={} status={} payload={}",
                                        participant.gameId,
                                        participant.playerId,
                                        session.id,
                                        action.status.code,
                                        action.payload,
                                    )
                                    session
                                        .send(Mono.just(session.textMessage(action.payload)))
                                        .then(session.close(action.status))
                                }
                            }
                        }.onErrorResume { exception ->
                            logger.error("Unhandled websocket error in game {}", gameId, exception)
                            session.close(CloseStatus.SERVER_ERROR)
                        }.doFinally {
                            logger.info(
                                "WebSocket disconnected gameId={} playerId={} sessionId={} signal={}",
                                participant.gameId,
                                participant.playerId,
                                session.id,
                                it,
                            )
                            sessionsByGameId[gameId]?.remove(session)
                            if (sessionsByGameId[gameId]?.isEmpty() == true) {
                                sessionsByGameId.remove(gameId)
                            }
                        }.then(),
                )
        }.onErrorResume(SessionAuthenticationException::class.java) { exception ->
            logger.warn("Rejected websocket session: {}", exception.message)
            session.close(exception.status)
        }.onErrorResume(NumberFormatException::class.java) { exception ->
            logger.warn("Failed to extract game id from websocket path {}", session.handshakeInfo.uri.path, exception)
            session.close(CloseStatus.BAD_DATA)
        }

    private fun processMove(
        json: String,
        participant: AuthenticatedParticipant,
    ): Mono<SessionAction> {
        val clientMessage =
            try {
                this.json.decodeFromString<ClientMessage>(json)
            } catch (exception: SerializationException) {
                logger.warn("Invalid websocket payload for game {}: {}", participant.gameId, json, exception)
                return Mono.just(
                    SessionAction.Close(
                        status = CloseStatus.BAD_DATA,
                        payload =
                            errorPayload(
                                "Invalid move payload. Expected JSON object: ${
                                    this.json.encodeToString<ClientMessage>(ClientMessage.Move("e2e4"))
                                }",
                            ),
                    ),
                )
            }
        val moveMessage =
            when (clientMessage) {
                is ClientMessage.Move -> clientMessage
            }

        return mono {
            when (
                val result =
                    gameService.executeMove(
                        gameId = participant.gameId,
                        playerId = participant.playerId,
                        playerColor = participant.color,
                        moveUci = moveMessage.moveUci,
                    )
            ) {
                is MoveExecutionResult.Success -> {
                    SessionAction.Broadcast(serverMessagePayload(result.game.toMoveMessage()))
                }

                is MoveExecutionResult.GameOver -> {
                    SessionAction.Reply(errorPayload("Game is over: ${result.gameResult}"))
                }

                is MoveExecutionResult.NotYourTurn -> {
                    SessionAction.Reply(errorPayload("It is not your turn"))
                }

                is MoveExecutionResult.IllegalMove -> {
                    SessionAction.Reply(errorPayload("Illegal move: ${result.moveUci}"))
                }

                is MoveExecutionResult.GameNotFound -> {
                    SessionAction.Close(
                        status = CloseStatus.POLICY_VIOLATION,
                        payload = errorPayload("Game ${participant.gameId} does not exist"),
                    )
                }
            }
        }
    }

    private fun notifyGameStartIfReady(gameId: Long): Mono<Void> =
        mono {
            val isReady = gameService.isGameReady(gameId)
            val activeSessions = sessionsByGameId[gameId]?.count { it.isOpen } ?: 0
            isReady && activeSessions >= 2
        }.flatMap { shouldBroadcast ->
            if (shouldBroadcast) {
                val payload = serverMessagePayload(ServerMessage.GameStart)
                logger.info("WebSocket game start gameId={} payload={}", gameId, payload)
                broadcastToGame(gameId, payload)
            } else {
                Mono.empty()
            }
        }

    private fun broadcastToGame(
        gameId: Long,
        payload: String,
    ): Mono<Void> =
        Flux
            .fromIterable(sessionsByGameId[gameId] ?: emptySet())
            .filter { webSocketSession -> webSocketSession.isOpen }
            .concatMap { webSocketSession ->
                logger.info(
                    "WebSocket broadcast gameId={} sessionId={} payload={}",
                    gameId,
                    webSocketSession.id,
                    payload,
                )
                webSocketSession.send(Mono.just(webSocketSession.textMessage(payload)))
            }.then()

    private suspend fun authenticateSession(session: WebSocketSession): AuthenticatedParticipant {
        val gameId = extractGameId(session)
        val playerToken =
            UriComponentsBuilder
                .fromUri(session.handshakeInfo.uri)
                .build()
                .queryParams
                .getFirst("playerToken")
                ?.takeIf { it.isNotBlank() }
                ?: throw SessionAuthenticationException(CloseStatus.POLICY_VIOLATION, "Missing playerToken query parameter")

        return gameService.authenticateParticipant(gameId, playerToken)
            ?: throw SessionAuthenticationException(CloseStatus.POLICY_VIOLATION, "Invalid player token for game $gameId")
    }

    private fun extractGameId(session: WebSocketSession): Long =
        session.handshakeInfo.uri.path
            .split("/")
            .last()
            .toLong()

    private fun Game.toMoveMessage(): ServerMessage.Move =
        ServerMessage.Move(fen = fen, startFen = history.startFen, movesUci = history.movesUci)

    private fun errorPayload(message: String): String = serverMessagePayload(ServerMessage.Error(message))

    private fun serverMessagePayload(message: ServerMessage): String = json.encodeToString<ServerMessage>(message)

    private class SessionAuthenticationException(
        val status: CloseStatus,
        message: String,
    ) : RuntimeException(message)

    private sealed interface SessionAction {
        data class Broadcast(
            val payload: String,
        ) : SessionAction

        data class Reply(
            val payload: String,
        ) : SessionAction

        data class Close(
            val status: CloseStatus,
            val payload: String,
        ) : SessionAction
    }
}
