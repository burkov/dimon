package com.github.burkov.dimonserver.configuration

import org.springframework.util.MimeTypeUtils
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.RSocketFactory
import io.rsocket.RSocket
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import sun.print.CUPSPrinter.getServer
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties
import reactor.core.publisher.Mono
import org.springframework.boot.web.server.LocalServerPort
import java.net.URI




@Configuration
class ClientConfiguration {
    @LocalServerPort
    private val port: Int = 8080
    @Bean
    @Lazy
    fun rSocketRequester(rSocketStrategies: RSocketStrategies, rSocketProps: RSocketProperties): Mono<RSocketRequester> {
        return RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .connectWebSocket(getURI(rSocketProps))
    }

    private fun getURI(rSocketProps: RSocketProperties): URI {
        return URI.create(String.format("ws://localhost:%d%s", port, rSocketProps.server.mappingPath))
    }
}