package xyz.daaren.cheesse.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient() =
    HttpClient {
        val jsonConfiguration =
            Json {
                encodeDefaults = true
                isLenient = true
                coerceInputValues = true
                ignoreUnknownKeys = true
            }

        install(ContentNegotiation) {
            json(jsonConfiguration)
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(jsonConfiguration)
        }
        defaultRequest {
            host = "127.0.0.1"
            port = 8080
        }
    }
