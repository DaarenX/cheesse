package xyz.daaren.cheesse.api.game

import xyz.daaren.cheesse.api.CreateGameResponse
import xyz.daaren.cheesse.api.GameResponse
import xyz.daaren.cheesse.api.JoinGameResponse
import xyz.daaren.cheesse.domain.game.CreatedGame
import xyz.daaren.cheesse.domain.game.Game
import xyz.daaren.cheesse.domain.game.JoinedGame

internal fun Game.toResponse(): GameResponse =
    GameResponse(
        id = id,
        fen = fen,
        pgn = pgn,
        whitePlayerId = whiteSeat?.playerId,
        blackPlayerId = blackSeat?.playerId,
    )

internal fun CreatedGame.toResponse(): CreateGameResponse =
    CreateGameResponse(
        gameId = gameId,
        joinToken = joinToken.toString(),
        playerId = playerId,
        playerToken = playerToken.toString(),
        color = color,
    )

internal fun JoinedGame.toResponse(): JoinGameResponse =
    JoinGameResponse(
        gameId = gameId,
        playerId = playerId,
        playerToken = playerToken.toString(),
        color = color,
    )
