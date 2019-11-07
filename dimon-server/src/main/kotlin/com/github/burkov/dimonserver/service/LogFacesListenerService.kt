package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.*
import com.github.burkov.dimonserver.repository.JobsRepository
import com.moonlit.logfaces.api.LogFacesAPI
import com.moonlit.logfaces.api.LogFacesView
import com.moonlit.logfaces.server.criteria.EventAttribute
import com.moonlit.logfaces.server.criteria.Operation
import org.apache.log4j.spi.LoggingEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
class LogFacesListenerService(val params: LogFacesClientParams,
                              val jobsTableCacheService: JobsTableCacheService,
                              val jobsRepository: JobsRepository) {
    private val log = LoggerFactory.getLogger(LogFacesListenerService::class.java)
    private lateinit var warnsView: LogFacesView
    private val processor: DirectProcessor<JobEvent> = DirectProcessor.create()
    private val runningJobs = mutableListOf<JobDTO>()
    val stream: Flux<JobEvent> = Flux.from(processor)


    @PostConstruct
    private fun postConstruct() {
        jobsTableCacheService.addOnCacheUpdateListener {
            synchronized(runningJobs) {
                val completed = runningJobs.filter { jobsTableCacheService.lookupCached(it.id) == null }
                runningJobs.removeAll(completed)
                completed.forEach {
                    processor.onNext(JobEventCompleted(it))
                }
            }
        }
        reconnect()
        //FIXME implement watchdog
    }

    private fun addToRunningJobsList(jobDto: JobDTO) {
        synchronized(runningJobs) {
            runningJobs.add(jobDto)
        }
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
                logEvent?.toJobEvents()?.let { jobEvent ->
                    processor.onNext(jobEvent)
                }
            }
            activate()
        }
    }

    private fun LoggingEvent.toJobEvents(): JobEvent? {
        val message = this.message.toString()

        fun parseWorkerId(): String = message.substringAfter("JOB=").substringBefore(", ")
        fun parseParams(): String = message.substringAfter("params=").substringBefore(", ")
        fun parseRetryCount(): Int = message.substringAfter("retryCount=").substringBefore(", ").toIntOrNull() ?: -1
        fun parseId(): Long = message.substringAfter("id=").toLongOrNull() ?: -1L

        val id = parseId()
        if (id == -1L) {
            log.warn("Failed to parse log line, id is missing {}", message)
            return null
        }

        val job = jobsTableCacheService.lookupCached(id) ?: jobsRepository.findById(id).orElseGet {
            // job was executed already
            null
        }

        return when {
            job == null -> JobEventCompletedInstantly(JobDTO(
                    id = id,
                    workerId = parseWorkerId(),
                    params = parseParams(),
                    retry_count = parseRetryCount(),
                    dueTo = LocalDateTime.now()
            ))
            message.contains("Executing JOB=") -> {
                val dto = job.toDTO()
                addToRunningJobsList(dto)
                JobEventStarted(dto)
            }
            message.contains("Finishing JOB=") -> JobEventCompleted(job.toDTO())
            else -> null
        }
    }
}