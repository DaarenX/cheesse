package xyz.daaren.cheesse.domain.game

class GameNotFoundException(
    joinToken: String,
) : RuntimeException("Game with join token $joinToken was not found")

class GameFullException(
    gameId: Long,
) : RuntimeException("Game $gameId already has two players")
