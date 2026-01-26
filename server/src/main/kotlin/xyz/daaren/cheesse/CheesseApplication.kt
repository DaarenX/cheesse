package xyz.daaren.cheesse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CheesseApplication

fun main(args: Array<String>) {
    runApplication<CheesseApplication>(*args)
}
