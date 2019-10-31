package com.github.burkov.dimonserver.configuration

import org.springframework.boot.autoconfigure.rsocket.RSocketProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class ClientConfiguration {
    @Bean
    @Lazy
    fun rSocketRequester(rSocketStrategies: RSocketStrategies, rSocketProps: RSocketProperties): Mono<RSocketRequester> {
        return RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .connectWebSocket(getURI(rSocketProps))
    }

    private fun getURI(rSocketProps: RSocketProperties): URI {
        return URI.create(String.format("ws://localhost:8080/${rSocketProps.server.mappingPath}"))
    }
}