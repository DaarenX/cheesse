package xyz.daaren.cheesse.domain.game

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
open class GameNotFoundException(
    joinToken: String,
) : RuntimeException("Game with join token $joinToken was not found") {
    constructor(gameId: Long) : this("id $gameId")
}

@ResponseStatus(HttpStatus.CONFLICT)
class GameFullException(
    gameId: Long,
) : RuntimeException("Game $gameId already has two players")
