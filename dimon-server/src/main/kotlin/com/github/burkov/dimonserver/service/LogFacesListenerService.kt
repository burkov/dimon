package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.configuration.LogFacesClientParams
import com.github.burkov.dimonserver.model.JobDTO
import com.github.burkov.dimonserver.model.JobEventCompleted
import com.github.burkov.dimonserver.model.JobEventStarted
import com.github.burkov.dimonserver.model.JobEventWithDto
import com.moonlit.logfaces.api.LogFacesAPI
import com.moonlit.logfaces.api.LogFacesView
import com.moonlit.logfaces.server.criteria.EventAttribute
import com.moonlit.logfaces.server.criteria.Operation
import org.apache.log4j.spi.LoggingEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PostConstruct
import kotlin.reflect.full.primaryConstructor

@Service
class LogFacesListenerService(val params: LogFacesClientParams,
                              val dispatcherMonitorService: DispatcherMonitorService) {
    private val log = LoggerFactory.getLogger(LogFacesListenerService::class.java)
    private lateinit var warnsView: LogFacesView
    private val lfsClientIsDead = AtomicBoolean(false)

    @PostConstruct
    private fun postConstruct() {
        reconnect()
    }

    @Scheduled(fixedDelayString = "\${dimon.lfs.watchdogIntervalMs}")
    private fun watchdog() {
        if (lfsClientIsDead.getAndSet(true)) reconnect()
    }

    private fun disconnect() {
        if (::warnsView.isInitialized) {
            log.info("Disconnecting")
            warnsView.deactivate()
            Thread.sleep(1000)
            LogFacesAPI.closeConnection()
        }
    }

    private fun reconnect() {
        disconnect()
        log.info("Connecting to ${params.url}:${params.port}")
        val currentConnection = LogFacesAPI.openConnection(params.url, params.port, true, params.login, params.password)
        requireNotNull(currentConnection) { "LogFacesAPI.openConnection(..) returned null-pointer" }
        warnsView = currentConnection.createView("waeView").apply {
            criteriaFilter = LogFacesAPI.makeCriteria().apply {
                addRule()
                        .addCondition(EventAttribute.loggerLevel, Operation.emore, "INFO")
                        .addCondition(EventAttribute.domainName, Operation.`is`, "jetprofile.prod.ecs")
                        .addCondition(EventAttribute.loggerName, Operation.`is`, "jetprofile.dispatcher.Dispatcher")
            }
            addListener { logEvent ->
                lfsClientIsDead.set(false)
                logEvent?.parseEvent()?.let { jobEvent ->
                    dispatcherMonitorService.reportJobEvent(jobEvent)
                }
            }
            activate()
        }
    }

    fun LoggingEvent.parseEvent(): JobEventWithDto? {
        val message = this.message?.toString() ?: run {
            log.warn("Log message is null")
            return null
        }
        val eventConstructor = when {
            message.contains("Executing JOB=") -> JobEventStarted::class
            message.contains("Finishing JOB=") -> JobEventCompleted::class
            else -> null
        }?.primaryConstructor ?: return null
        val id = message.substringAfter(" id=").toLongOrNull() ?: run {
            log.warn("Failed to parse log line, id is missing {}", message)
            return null
        }

        return eventConstructor.call(JobDTO(
                id = id,
                workerId = message.substringAfter(" JOB=").substringBefore(", "),
                params = message.substringAfter(" params=").substringBefore(", "),
                retry_count = message.substringAfter(" retryCount=").substringBefore(", ").toIntOrNull() ?: -1,
                dueTo = LocalDateTime.now()
        ))
    }
}