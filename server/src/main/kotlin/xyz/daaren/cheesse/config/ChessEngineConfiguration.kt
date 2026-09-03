package xyz.daaren.cheesse.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.daaren.cheesse.domain.chess.ChessEngineAdapter
import xyz.daaren.cheesse.domain.chess.DefaultChessEngineAdapter

@Configuration
class ChessEngineConfiguration {
    @Bean
    fun chessEngineAdapter(): ChessEngineAdapter = DefaultChessEngineAdapter()
}
