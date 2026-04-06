package xyz.daaren.cheesse.domain.game

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.persistence.game.GameEntity
import xyz.daaren.cheesse.persistence.game.GameRepository
import xyz.daaren.cheesse.persistence.game.toDomainModel
import java.security.SecureRandom
import java.util.UUID

@Service
class GameService(
    private val gameRepository: GameRepository,
) {
    private val secureRandom = SecureRandom()

    suspend fun findGame(id: Long): Game? = gameRepository.findById(id)?.toDomainModel()

    @Transactional
    suspend fun createGame(colorPreference: GameColorPreference = GameColorPreference.WHITE): CreatedGame {
        val creatorColor =
            when (colorPreference) {
                GameColorPreference.WHITE -> PlayerColor.WHITE
                GameColorPreference.BLACK -> PlayerColor.BLACK
                GameColorPreference.RANDOM -> if (secureRandom.nextBoolean()) PlayerColor.WHITE else PlayerColor.BLACK
            }
        val creatorSeat =
            PlayerSeat(
                playerId = nextPlayerId(),
                playerToken = newToken(),
            )
        val savedGame =
            gameRepository.save(
                GameEntity(
                    joinToken = newToken(),
                    whitePlayerId = creatorSeat.playerId.takeIf { creatorColor == PlayerColor.WHITE },
                    whitePlayerToken = creatorSeat.playerToken.takeIf { creatorColor == PlayerColor.WHITE },
                    blackPlayerId = creatorSeat.playerId.takeIf { creatorColor == PlayerColor.BLACK },
                    blackPlayerToken = creatorSeat.playerToken.takeIf { creatorColor == PlayerColor.BLACK },
                ),
            )
        val gameId = savedGame.id ?: error("Saved game has no id")

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
        val game = gameRepository.findByJoinToken(token)?.toDomainModel() ?: throw GameNotFoundException(token)
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

        val joiningSeat =
            PlayerSeat(
                playerId = nextPlayerId(),
                playerToken = newToken(),
            )
        val updatedRows =
            when (joinerColor) {
                PlayerColor.WHITE ->
                    gameRepository.assignWhitePlayer(
                        gameId = game.id,
                        playerId = joiningSeat.playerId,
                        playerToken = joiningSeat.playerToken,
                    )

                PlayerColor.BLACK ->
                    gameRepository.assignBlackPlayer(
                        gameId = game.id,
                        playerId = joiningSeat.playerId,
                        playerToken = joiningSeat.playerToken,
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

    private fun newToken(): String = UUID.randomUUID().toString()

    private fun nextPlayerId(): Long = secureRandom.nextLong(1, Long.MAX_VALUE)
}
