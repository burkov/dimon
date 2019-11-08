package com.github.burkov.dimonserver.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("dimon.lfs")
data class LogFacesClientParams(
        val login: String,
        val password: String,
        val url: String,
        val port: Int
)