package xyz.daaren.cheesse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
