package xyz.daaren.cheesse.persistence.game

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface GameRepository : CoroutineCrudRepository<GameEntity, Long> {
    @Query("SELECT * FROM games WHERE join_token = :joinToken")
    suspend fun findByJoinToken(joinToken: String): GameEntity?

    @Modifying
    @Query(
        """
        UPDATE games
        SET black_player_id = :playerId, black_player_token = :playerToken
        WHERE id = :gameId AND black_player_id IS NULL
        """,
    )
    suspend fun assignBlackPlayer(
        gameId: Long,
        playerId: Long,
        playerToken: String,
    ): Int

    @Modifying
    @Query(
        """
        UPDATE games
        SET white_player_id = :playerId, white_player_token = :playerToken
        WHERE id = :gameId AND white_player_id IS NULL
        """,
    )
    suspend fun assignWhitePlayer(
        gameId: Long,
        playerId: Long,
        playerToken: String,
    ): Int
}
