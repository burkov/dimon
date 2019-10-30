package com.github.burkov.dimonserver.controller

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.protobuf.GreeterGrpc
import com.github.burkov.dimonserver.protobuf.Hello
import com.github.burkov.dimonserver.protobuf.Hello.HelloRequest
import com.github.burkov.dimonserver.repository.JobsRepository
import com.github.burkov.dimonserver.service.GrpcClientService
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api")
class JobsRestController(val jobsRepository: JobsRepository, val grpcService: GrpcClientService) {
    @GetMapping("/jobs")
    fun getAllJobs(): List<Job> = jobsRepository.findAll()

    @GetMapping("/jobs/contexts")
    fun getAllContexts(): Set<String> = jobsRepository.listDistinctContexts()

    @GetMapping("/test")
    fun testGrpcCall(name: String): String? {
        return grpcService.sendMessage(name)
    }
}

@GrpcService
class JobsStreamController : GreeterGrpc.GreeterImplBase() {
    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<Hello.HelloResponse>) {
        val reply = Hello.HelloResponse.newBuilder().setMessage("Hello ==> " + request.name).build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }
}

