package xyz.daaren.cheesse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.daaren.cheesse.Greeting

@SpringBootApplication
class CheesseApplication

fun main(args: Array<String>) {
    runApplication<CheesseApplication>(*args)
}

@RestController
class DummyController {

    @GetMapping("/")
    fun hello(): String {
        return "Spring Boot: ${Greeting().greet()}"
    }
}
