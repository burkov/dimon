package com.github.burkov.dpmonserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DpmonServerApplication

fun main(args: Array<String>) {
    runApplication<DpmonServerApplication>(*args)
}
