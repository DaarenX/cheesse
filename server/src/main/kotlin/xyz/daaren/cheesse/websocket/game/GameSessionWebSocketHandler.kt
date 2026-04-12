package xyz.daaren.cheesse.websocket.game

import io.github.alluhemanth.chess.core.ChessGame
import io.github.alluhemanth.chess.core.board.Square
import io.github.alluhemanth.chess.core.move.Move
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import xyz.daaren.cheesse.api.game.toResponse
import xyz.daaren.cheesse.domain.game.PlayerColor
import xyz.daaren.cheesse.persistence.game.GameRepository
import xyz.daaren.cheesse.persistence.game.toDomainModel
import java.util.concurrent.ConcurrentHashMap

@Component
class GameSessionWebSocketHandler(
    private val gameRepository: GameRepository,
    private val objectMapper: ObjectMapper,
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(GameSessionWebSocketHandler::class.java)
    private val sessionsByGameId = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    override fun handle(session: WebSocketSession): Mono<Void> =
        mono {
            authenticateSession(session)
        }.flatMap { participant ->
            val gameId = participant.gameId
            val sessionsForGame = sessionsByGameId.computeIfAbsent(gameId) { ConcurrentHashMap.newKeySet() }
            sessionsForGame.add(session)

            session
                .receive()
                .concatMap { message ->
                    processMove(message.payloadAsText, participant)
                }.concatMap { action ->
                    when (action) {
                        is SessionAction.Broadcast -> {
                            Flux
                                .fromIterable(sessionsByGameId[gameId] ?: emptySet())
                                .filter { webSocketSession -> webSocketSession.isOpen }
                                .concatMap { webSocketSession ->
                                    webSocketSession.send(Mono.just(webSocketSession.textMessage(action.payload)))
                                }.then()
                        }

                        is SessionAction.Reply -> {
                            session.send(Mono.just(session.textMessage(action.payload))).then()
                        }

                        is SessionAction.Close -> {
                            session
                                .send(Mono.just(session.textMessage(action.payload)))
                                .then(session.close(action.status))
                        }
                    }
                }.onErrorResume { exception ->
                    logger.error("Unhandled websocket error in game {}", gameId, exception)
                    session.close(CloseStatus.SERVER_ERROR)
                }.doFinally {
                    sessionsByGameId[gameId]?.remove(session)
                    if (sessionsByGameId[gameId]?.isEmpty() == true) {
                        sessionsByGameId.remove(gameId)
                    }
                }.then()
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
        val moveMessage =
            try {
                objectMapper.readValue(json, GameMoveMessage::class.java)
            } catch (exception: Exception) {
                logger.warn("Invalid websocket payload for game {}: {}", participant.gameId, json, exception)
                return Mono.just(
                    SessionAction.Close(
                        status = CloseStatus.BAD_DATA,
                        payload =
                            errorPayload(
                                "Invalid move payload. Expected JSON object: ${
                                    objectMapper.writeValueAsString(GameMoveMessage("e2e4"))
                                }",
                            ),
                    ),
                )
            }

        return mono {
            val currentGameEntity =
                gameRepository.findById(participant.gameId)
                    ?: return@mono SessionAction.Close(
                        status = CloseStatus.POLICY_VIOLATION,
                        payload = errorPayload("Game ${participant.gameId} does not exist"),
                    )
            val currentGame = currentGameEntity.toDomainModel()

            if (activePlayerColor(currentGame.fen) != participant.color) {
                return@mono SessionAction.Reply(errorPayload("It is not your turn"))
            }

            val chessGame = ChessGame().apply { loadFen(currentGame.fen) }
            val isValidMove =
                try {
                    chessGame.makeUciMoveWorkaround(moveMessage.moveUci)
                } catch (_: IllegalArgumentException) {
                    false
                }

            if (!isValidMove) {
                return@mono SessionAction.Reply(errorPayload("Illegal move: ${moveMessage.moveUci}"))
            }

            val savedGame =
                gameRepository
                    .save(
                        currentGameEntity.copy(
                            fen = chessGame.getFen(),
                            pgn = chessGame.getPgn(),
                        ),
                    ).toDomainModel()

            SessionAction.Broadcast(objectMapper.writeValueAsString(savedGame.toResponse()))
        }
    }

    private fun ChessGame.makeUciMoveWorkaround(uci: String): Boolean {
        val fromSquare = Square(uci.substring(0, 2))
        val toSquare = Square(uci.substring(2, 4))
        val promotionChar = uci.getOrNull(4)
        val requestedMove = Move(fromSquare, toSquare, promotionChar)
        val legalMove =
            getLegalMoves().firstOrNull {
                it.from == requestedMove.from &&
                    it.to == requestedMove.to &&
                    it.promotionPieceType == requestedMove.promotionPieceType
            } ?: return false

        return makeMove(legalMove)
    }

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
        val game =
            gameRepository.findById(gameId)?.toDomainModel()
                ?: throw SessionAuthenticationException(CloseStatus.POLICY_VIOLATION, "Game $gameId does not exist")

        return when (playerToken) {
            game.whiteSeat?.playerToken ->
                AuthenticatedParticipant(
                    gameId = gameId,
                    playerId = game.whiteSeat.playerId,
                    color = PlayerColor.WHITE,
                )

            game.blackSeat?.playerToken ->
                AuthenticatedParticipant(
                    gameId = gameId,
                    playerId = game.blackSeat.playerId,
                    color = PlayerColor.BLACK,
                )

            else -> throw SessionAuthenticationException(CloseStatus.POLICY_VIOLATION, "Invalid player token for game $gameId")
        }
    }

    private fun extractGameId(session: WebSocketSession): Long =
        session.handshakeInfo.uri.path
            .split("/")
            .last()
            .toLong()

    private fun activePlayerColor(fen: String): PlayerColor {
        val parts = fen.split(" ")
        return if (parts.getOrNull(1) == "b") PlayerColor.BLACK else PlayerColor.WHITE
    }

    private fun errorPayload(message: String): String = objectMapper.writeValueAsString(mapOf("type" to "error", "message" to message))

    private data class AuthenticatedParticipant(
        val gameId: Long,
        val playerId: Long,
        val color: PlayerColor,
    )

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
