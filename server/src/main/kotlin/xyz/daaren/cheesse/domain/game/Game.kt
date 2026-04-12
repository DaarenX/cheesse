package xyz.daaren.cheesse.domain.game

data class Game(
    val id: Long,
    val fen: String,
    val pgn: String,
    val joinToken: String,
    val whiteSeat: PlayerSeat?,
    val blackSeat: PlayerSeat?,
)

data class PlayerSeat(
    val playerId: Long,
    val playerToken: String,
)
