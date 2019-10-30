package com.github.burkov.dimonserver

import com.github.burkov.dimonserver.protobuf.GreeterGrpc
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DpmonServerApplication

fun main(args: Array<String>) {
    runApplication<DpmonServerApplication>(*args)
}
