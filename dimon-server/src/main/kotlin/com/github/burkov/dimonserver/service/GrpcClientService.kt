package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.protobuf.GreeterGrpc
import io.grpc.StatusRuntimeException
import com.github.burkov.dimonserver.protobuf.Hello.HelloRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service


@Service
class GrpcClientService {
    @GrpcClient("localClient")
    val client: GreeterGrpc.GreeterBlockingStub? = null

    fun sendMessage(name: String): String {
        return try {
            val response = this.client!!.sayHello(HelloRequest.newBuilder().setName(name).build())
            response.message
        } catch (e: StatusRuntimeException) {
            e.printStackTrace()
            "FAILED with ${e.stackTrace.joinToString("\n")}"
        }
    }
}