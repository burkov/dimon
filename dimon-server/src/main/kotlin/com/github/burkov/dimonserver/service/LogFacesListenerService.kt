package com.github.burkov.dimonserver.service

import com.moonlit.logfaces.api.LogFacesView
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class LogFacesListenerService {
    private val log = LoggerFactory.getLogger(LogFacesListenerService::class.java)
    private lateinit var warnsView: LogFacesView

    @PostConstruct
    fun postConstruct() {
        reconnect()

    }

    private fun disconnect() {
        log.info("Disconnecting")
        //        warnsView?.deactivate()
//        dispatcherStateView?.deactivate()
//        Thread.sleep(500)
//        LogFacesAPI.closeConnection()
    }

    private fun reconnect() {
        disconnect()
        log.info("Connecting")
//        currentConnection = LogFacesAPI.openConnection(
//                AppConfig.LFS.Connection.url,
//                AppConfig.LFS.Connection.port,
//                true,
//                AppConfig.LFS.Connection.login,
//                AppConfig.LFS.Connection.password)

    }
}
//class LfsLogProvider : LogProvider() {
//    init {
//        enableWatchdog()
//    }
//    override fun reconnect() {
//        logger.info("Initializing connection to log provider")

//        requireNotNull(currentConnection) { "LogFacesAPI.openConnection(..) returned null-pointer" }
//        val warnsFilter = LogFacesAPI.makeCriteria().apply {
//            addRule()
//                    .addCondition(EventAttribute.loggerLevel, Operation.emore, AppConfig.LFS.Filter.logLevel)
//                    .addCondition(EventAttribute.domainName, Operation.`is`, AppConfig.LFS.Filter.domain)
//        }
//        val warnsQuery = currentConnection!!.createQuery("waeQuery").apply {
//            setCriteria(warnsFilter)
//            setMaxSize(AppConfig.keepLastNLogLines)
//            setOrder(Order.DESCENDING)
//        }
//        putAllLogEvents(warnsQuery.results.map { le ->
//            LogEvent.fromLog4jLoggingEvent(le)
//        })
//
//        activateWarnsView(warnsFilter)
////        activateDispatcherStateView()
//        logger.info("Log provider connected")
//    }
//
//    private fun activateDispatcherStateView() {
//        dispatcherStateView = currentConnection!!.createView("dispatcherState").apply {
//            criteriaFilter = LogFacesAPI.makeCriteria().apply {
//                addRule().addCondition(EventAttribute.loggerLevel, Operation.`is`, LogLevel.INFO.name)
//                        .addCondition(EventAttribute.domainName, Operation.`is`, AppConfig.LFS.Filter.domain)
//                        .addCondition(EventAttribute.loggerName, Operation.`is`, "JobHealth")
//            }
//            addListener { le ->
//                val msg = LogEvent.fromLog4jLoggingEvent(le).message
//                when {
//                    msg.startsWith("failed_jobs: ") -> DispatcherStats.reportFailedJobs(msg)
//                    msg.startsWith("pending_jobs: ") -> DispatcherStats.reportPendingJobs(msg)
//                }
//            }
//            activate()
//        }
//    }
//
//    private fun activateWarnsView(warnsFilter: CriteriaFilter) {
//        warnsView = currentConnection!!.createView("waeView").apply {
//            criteriaFilter = warnsFilter
//            addListener { le ->
//                putLogEvent(LogEvent.fromLog4jLoggingEvent(le))
//            }
//            activate()
//        }
//    }
//

//}%