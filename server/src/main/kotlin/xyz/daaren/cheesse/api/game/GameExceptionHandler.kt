package xyz.daaren.cheesse.api.game

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import xyz.daaren.cheesse.domain.game.GameFullException
import xyz.daaren.cheesse.domain.game.GameNotFoundException

@RestControllerAdvice
class GameExceptionHandler {
    private val logger = LoggerFactory.getLogger(GameExceptionHandler::class.java)

    @ExceptionHandler(GameNotFoundException::class)
    fun handleNotFound(exception: GameNotFoundException): ResponseEntity<Map<String, String>> {
        logger.debug("Game not found: {}", exception.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (exception.message ?: "Not Found")))
    }

    @ExceptionHandler(GameFullException::class)
    fun handleGameFull(exception: GameFullException): ResponseEntity<Map<String, String>> {
        logger.debug("Game conflict: {}", exception.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to (exception.message ?: "Conflict")))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        logger.debug("Bad request: {}", exception.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to (exception.message ?: "Bad Request")))
    }
}
