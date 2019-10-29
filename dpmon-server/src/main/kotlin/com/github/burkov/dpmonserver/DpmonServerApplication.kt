package com.github.burkov.dpmonserver

import com.github.burkov.dpmonserver.protobuf.GreeterGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.concurrent.thread

@SpringBootApplication
class DpmonServerApplication

fun main(args: Array<String>) {
    runApplication<DpmonServerApplication>(*args)
}
