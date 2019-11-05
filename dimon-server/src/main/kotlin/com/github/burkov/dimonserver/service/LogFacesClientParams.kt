package com.github.burkov.dimonserver.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class LogFacesClientParams(
        @Value("\${dimon.lfs.login}") val login: String,
        @Value("\${dimon.lfs.password}") val password: String,
        @Value("\${dimon.lfs.url}") val url: String,
        @Value("\${dimon.lfs.port}") val port: Int
)