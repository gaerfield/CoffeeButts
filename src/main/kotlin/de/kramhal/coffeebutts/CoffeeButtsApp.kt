package de.kramhal.coffeebutts

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoffeeButtsApp

fun main(args: Array<String>) {
    runApplication<CoffeeButtsApp>(*args)
}