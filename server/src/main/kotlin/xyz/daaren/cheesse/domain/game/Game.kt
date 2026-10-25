package xyz.daaren.cheesse.domain.game

import xyz.daaren.cheesse.domain.chess.ChessGameHistory
import java.util.UUID

data class Game(
    val id: Long,
    val fen: String,
    val pgn: String,
    val history: ChessGameHistory,
    val joinToken: UUID,
    val whiteSeat: PlayerSeat?,
    val blackSeat: PlayerSeat?,
)

data class PlayerSeat(
    val playerId: Long,
    val playerToken: UUID,
)
