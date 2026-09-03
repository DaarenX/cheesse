package xyz.daaren.cheesse.domain.game

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessEngineAdapter
import xyz.daaren.cheesse.domain.chess.ChessMoveResult
import xyz.daaren.cheesse.persistence.game.GameEntity
import xyz.daaren.cheesse.persistence.game.GameRepository
import xyz.daaren.cheesse.persistence.game.toDomainModel
import xyz.daaren.cheesse.persistence.game.withHistory
import java.security.SecureRandom
import java.util.UUID

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val chessEngineAdapter: ChessEngineAdapter,
) {
    private val logger = LoggerFactory.getLogger(GameService::class.java)
    private val secureRandom = SecureRandom()

    suspend fun findGame(id: Long): Game? = gameRepository.findById(id)?.toDomainModel()

    suspend fun getGame(id: Long): Game = findGame(id) ?: throw GameNotFoundException(id)

    @Transactional
    suspend fun createGame(colorPreference: GameColorPreference = GameColorPreference.WHITE): CreatedGame {
        val creatorColor =
            when (colorPreference) {
                GameColorPreference.WHITE -> PlayerColor.WHITE
                GameColorPreference.BLACK -> PlayerColor.BLACK
                GameColorPreference.RANDOM -> if (secureRandom.nextBoolean()) PlayerColor.WHITE else PlayerColor.BLACK
            }
        val creatorToken = newToken()
        val creatorSeat =
            PlayerSeat(
                playerId = nextPlayerId(),
                playerToken = creatorToken,
            )
        val joinToken = newToken()
        val savedGame =
            gameRepository.save(
                GameEntity(
                    joinToken = joinToken,
                    whitePlayerId = creatorSeat.playerId.takeIf { creatorColor == PlayerColor.WHITE },
                    whitePlayerToken = creatorToken.takeIf { creatorColor == PlayerColor.WHITE },
                    blackPlayerId = creatorSeat.playerId.takeIf { creatorColor == PlayerColor.BLACK },
                    blackPlayerToken = creatorToken.takeIf { creatorColor == PlayerColor.BLACK },
                ),
            )
        val gameId = savedGame.id ?: error("Saved game has no id")

        logger.info("game created id: ${savedGame.id} joinToken: $joinToken")

        return CreatedGame(
            gameId = gameId,
            joinToken = savedGame.joinToken,
            playerId = creatorSeat.playerId,
            playerToken = creatorSeat.playerToken,
            color = creatorColor,
        )
    }

    @Transactional
    suspend fun joinGame(token: String): JoinedGame {
        val joinToken = parseToken(token) ?: throw GameNotFoundException(token)
        val game = gameRepository.findByJoinToken(joinToken)?.toDomainModel() ?: throw GameNotFoundException(token)
        val joinerColor =
            when {
                game.whiteSeat == null -> PlayerColor.WHITE
                game.blackSeat == null -> PlayerColor.BLACK
                else -> throw GameFullException(game.id)
            }

        requireNotNull(
            when (joinerColor) {
                PlayerColor.WHITE -> game.blackSeat
                PlayerColor.BLACK -> game.whiteSeat
            },
        ) { "Game ${game.id} is missing the opposing player data" }

        val joiningToken = newToken()
        val joiningSeat =
            PlayerSeat(
                playerId = nextPlayerId(),
                playerToken = joiningToken,
            )
        val updatedRows =
            when (joinerColor) {
                PlayerColor.WHITE ->
                    gameRepository.assignWhitePlayer(
                        gameId = game.id,
                        playerId = joiningSeat.playerId,
                        playerToken = joiningToken,
                    )

                PlayerColor.BLACK ->
                    gameRepository.assignBlackPlayer(
                        gameId = game.id,
                        playerId = joiningSeat.playerId,
                        playerToken = joiningToken,
                    )
            }

        if (updatedRows != 1) {
            throw GameFullException(game.id)
        }

        return JoinedGame(
            gameId = game.id,
            playerId = joiningSeat.playerId,
            playerToken = joiningSeat.playerToken,
            color = joinerColor,
        )
    }

    suspend fun isGameReady(gameId: Long): Boolean {
        val game = gameRepository.findById(gameId) ?: return false
        return game.whitePlayerId != null && game.blackPlayerId != null
    }

    suspend fun authenticateParticipant(
        gameId: Long,
        playerToken: String,
    ): AuthenticatedParticipant? {
        val game = findGame(gameId) ?: return null
        return when (playerToken) {
            game.whiteSeat?.playerToken?.toString() ->
                AuthenticatedParticipant(
                    gameId = gameId,
                    playerId = game.whiteSeat.playerId,
                    color = PlayerColor.WHITE,
                )

            game.blackSeat?.playerToken?.toString() ->
                AuthenticatedParticipant(
                    gameId = gameId,
                    playerId = game.blackSeat.playerId,
                    color = PlayerColor.BLACK,
                )

            else -> null
        }
    }

    suspend fun executeMove(
        gameId: Long,
        playerId: Long,
        playerColor: PlayerColor,
        moveUci: String,
    ): MoveExecutionResult {
        val currentGameEntity = gameRepository.findById(gameId) ?: return MoveExecutionResult.GameNotFound
        val currentGame = currentGameEntity.toDomainModel()

        if (chessEngineAdapter.activePlayerColor(currentGame.fen) != playerColor) {
            return MoveExecutionResult.NotYourTurn
        }

        return when (val moveResult = chessEngineAdapter.applyMove(currentGame.history, moveUci)) {
            is ChessMoveResult.IllegalMove -> MoveExecutionResult.IllegalMove(moveUci)
            is ChessMoveResult.GameOver -> MoveExecutionResult.GameOver(moveResult.gameResult)
            is ChessMoveResult.Success -> {
                val savedGame =
                    gameRepository
                        .save(currentGameEntity.withHistory(moveResult.history, moveResult.fen, moveResult.pgn))
                        .toDomainModel()
                MoveExecutionResult.Success(savedGame)
            }
        }
    }

    private fun newToken(): UUID = UUID.randomUUID()

    private fun parseToken(token: String): UUID? = runCatching { UUID.fromString(token) }.getOrNull()

    private fun nextPlayerId(): Long = secureRandom.nextLong(1, Long.MAX_VALUE)
}
