package xyz.daaren.cheesse.api.game

import xyz.daaren.cheesse.api.game.dto.CreateGameResponse
import xyz.daaren.cheesse.api.game.dto.GameResponse
import xyz.daaren.cheesse.api.game.dto.JoinGameResponse
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
        joinToken = joinToken,
        playerId = playerId,
        playerToken = playerToken,
        color = color,
    )

internal fun JoinedGame.toResponse(): JoinGameResponse =
    JoinGameResponse(
        gameId = gameId,
        playerId = playerId,
        playerToken = playerToken,
        color = color,
    )
