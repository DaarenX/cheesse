package xyz.daaren.cheesse.domain.game

data class CreatedGame(
    val gameId: Long,
    val joinToken: String,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)

data class JoinedGame(
    val gameId: Long,
    val playerId: Long,
    val playerToken: String,
    val color: PlayerColor,
)

enum class PlayerColor {
    WHITE,
    BLACK,
}

enum class GameColorPreference {
    WHITE,
    BLACK,
    RANDOM,
}
