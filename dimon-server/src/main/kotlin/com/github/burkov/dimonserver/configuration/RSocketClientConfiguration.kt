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

@Configuration
class ClientConfiguration {
    @Bean
    @Lazy
    fun rSocketRequester(rSocketStrategies: RSocketStrategies): RSocketRequester {
        return RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .connectTcp("localhost", 7000)
                .block()!!
    }
}