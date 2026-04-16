package xyz.daaren.cheesse.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import java.util.concurrent.TimeUnit

@Configuration
class RequestLoggingConfiguration {
    private val logger = LoggerFactory.getLogger(RequestLoggingConfiguration::class.java)

    @Bean
    fun requestLoggingFilter(): WebFilter =
        WebFilter { exchange: ServerWebExchange, chain ->
            val startNanos = System.nanoTime()
            val request = exchange.request
            val method = request.method.name()
            val path = request.uri.path
            val query =
                request.uri.rawQuery
                    ?.let { "?$it" }
                    .orEmpty()

            logger.info("HTTP {} {}{} started", method, path, query)

            chain
                .filter(exchange)
                .doOnSuccess {
                    logger.info(
                        "HTTP {} {}{} completed status={} durationMs={}",
                        method,
                        path,
                        query,
                        exchange.response.statusCode?.value() ?: 200,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos),
                    )
                }.doOnError { exception ->
                    logger.error(
                        "HTTP {} {}{} failed after {}ms",
                        method,
                        path,
                        query,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos),
                        exception,
                    )
                }
        }
}
