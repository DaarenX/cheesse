package xyz.daaren.cheesse.api.game

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import xyz.daaren.cheesse.api.CreateGameRequest
import xyz.daaren.cheesse.api.CreateGameResponse
import xyz.daaren.cheesse.api.GameResponse
import xyz.daaren.cheesse.api.JoinGameRequest
import xyz.daaren.cheesse.api.JoinGameResponse
import xyz.daaren.cheesse.domain.game.GameFullException
import xyz.daaren.cheesse.domain.game.GameNotFoundException
import xyz.daaren.cheesse.domain.game.GameService

@RestController
@RequestMapping("/games")
class GameController(
    private val gameService: GameService,
) {
    @GetMapping("/{id}")
    suspend fun getGame(
        @PathVariable("id") id: Long,
    ): GameResponse =
        gameService.findGame(id)?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No game with id found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createGame(
        @RequestBody request: CreateGameRequest,
    ): CreateGameResponse = gameService.createGame(request.color).toResponse()

    @PostMapping("/join")
    suspend fun joinGame(
        @RequestBody request: JoinGameRequest,
    ): JoinGameResponse =
        try {
            gameService.joinGame(request.token).toResponse()
        } catch (_: GameNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Game with token is not available")
        } catch (_: GameFullException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Game already has two players")
        }
}
