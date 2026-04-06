package xyz.daaren.cheesse.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class CorsConfiguration : WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*")
    }
}
