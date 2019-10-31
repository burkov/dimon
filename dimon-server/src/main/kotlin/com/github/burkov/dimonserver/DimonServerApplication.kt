package com.github.burkov.dimonserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DimonServerApplication

fun main(args: Array<String>) {
    runApplication<DimonServerApplication>(*args)
}
