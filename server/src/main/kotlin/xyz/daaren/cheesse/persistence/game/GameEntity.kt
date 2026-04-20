package xyz.daaren.cheesse.persistence.game

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

private const val START_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

@Table("games")
data class GameEntity(
    @Id val id: Long? = null,
    val fen: String = START_POSITION_FEN,
    val pgn: String = "",
    val joinToken: UUID,
    val whitePlayerId: Long? = null,
    val whitePlayerToken: UUID? = null,
    val blackPlayerId: Long? = null,
    val blackPlayerToken: UUID? = null,
)
