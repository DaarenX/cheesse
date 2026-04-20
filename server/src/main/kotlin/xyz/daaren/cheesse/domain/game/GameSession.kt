package xyz.daaren.cheesse.domain.game

import xyz.daaren.cheesse.api.PlayerColor
import java.util.UUID

data class CreatedGame(
    val gameId: Long,
    val joinToken: UUID,
    val playerId: Long,
    val playerToken: UUID,
    val color: PlayerColor,
)

data class JoinedGame(
    val gameId: Long,
    val playerId: Long,
    val playerToken: UUID,
    val color: PlayerColor,
)
