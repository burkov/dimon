package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.JobEvent
import com.github.burkov.dimonserver.model.JobEventFinish
import com.github.burkov.dimonserver.model.JobEventStart
import com.moonlit.logfaces.api.LogFacesAPI
import com.moonlit.logfaces.api.LogFacesView
import com.moonlit.logfaces.server.criteria.EventAttribute
import com.moonlit.logfaces.server.criteria.Operation
import org.apache.log4j.spi.LoggingEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import javax.annotation.PostConstruct

@Service
class LogFacesListenerService(val params: LogFacesClientParams) {
    private val log = LoggerFactory.getLogger(LogFacesListenerService::class.java)
    private lateinit var warnsView: LogFacesView
    private val processor: DirectProcessor<JobEvent> = DirectProcessor.create()
    val stream: Flux<JobEvent> = Flux.from(processor)

    @PostConstruct
    private fun postConstruct() {
        reconnect()
        //FIXME implement watchdog
    }

    private fun disconnect() {
        if (::warnsView.isInitialized) {
            log.info("Disconnecting")
            warnsView.deactivate()
            Thread.sleep(500)
            LogFacesAPI.closeConnection()
        }
    }

    private fun reconnect() {
        disconnect()
        log.info("Connecting to ${params.url}")
        val currentConnection = LogFacesAPI.openConnection(
                params.url,
                params.port,
                true,
                params.login,
                params.password)
        requireNotNull(currentConnection) { "LogFacesAPI.openConnection(..) returned null-pointer" }
        val warnsFilter = LogFacesAPI.makeCriteria().apply {
            addRule()
                    .addCondition(EventAttribute.loggerLevel, Operation.emore, "INFO")
                    .addCondition(EventAttribute.domainName, Operation.`is`, "jetprofile.prod.ecs")
                    .addCondition(EventAttribute.loggerName, Operation.`is`, "jetprofile.dispatcher.Dispatcher")
        }
        warnsView = currentConnection.createView("waeView").apply {
            criteriaFilter = warnsFilter
            addListener { logEvent ->
                logEvent?.let { parseLogEvent(it)?.let { processor.onNext(it) } }
            }
            activate()
        }
    }

    private fun parseLogEvent(logEvent: LoggingEvent): JobEvent? {
        fun parseWorkerId(message: String): String = message.substringAfter("JOB=").substringBefore(", ")
        fun parseParams(message: String): String = message.substringAfter("params=").substringBefore(", ")
        fun parseRetryCount(message: String): Int = message.substringAfter("retryCount=").toIntOrNull() ?: -1
        fun parseExecutingEvent(message: String): JobEvent = JobEventStart(
                workerId = parseWorkerId(message),
                params = parseParams(message),
                retryCount = parseRetryCount(message)
        )

        fun parseFinishingEvent(message: String): JobEvent = JobEventFinish(
                workerId = parseWorkerId(message),
                params = parseParams(message),
                retryCount = parseRetryCount(message)
        )

        val message = logEvent.message.toString()
        return when {
            message.contains("Executing JOB=") -> parseExecutingEvent(message)
            message.contains("Finishing JOB=") -> parseFinishingEvent(message)
            else -> null
        }
    }
}