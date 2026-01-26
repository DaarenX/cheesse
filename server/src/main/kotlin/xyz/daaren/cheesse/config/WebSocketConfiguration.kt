package xyz.daaren.cheesse.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import xyz.daaren.cheesse.websocket.game.GameSessionWebSocketHandler

@Configuration
class WebSocketConfiguration(
    private val gameSessionWebSocketHandler: GameSessionWebSocketHandler,
) {
    @Bean
    fun webSocketHandlerMapping(): HandlerMapping =
        SimpleUrlHandlerMapping().apply {
            order = 1
            urlMap = mapOf("/ws/game-session/{id}" to gameSessionWebSocketHandler)
        }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter = WebSocketHandlerAdapter()
}
