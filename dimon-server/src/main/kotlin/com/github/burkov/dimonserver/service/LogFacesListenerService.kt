package com.github.burkov.dimonserver.service

import com.moonlit.logfaces.api.LogFacesAPI
import com.moonlit.logfaces.api.LogFacesView
import com.moonlit.logfaces.server.core.LogEvent
import com.moonlit.logfaces.server.core.Order
import com.moonlit.logfaces.server.criteria.EventAttribute
import com.moonlit.logfaces.server.criteria.Operation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class LogFacesListenerService(val params: LogFacesClientParams) {
    private val log = LoggerFactory.getLogger(LogFacesListenerService::class.java)
    private lateinit var warnsView: LogFacesView


    @PostConstruct
    fun postConstruct() {
        reconnect()
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
            //                        .addCondition(EventAttribute.loggerName, Operation.`is`, "JobHealth")
        }
//        val warnsQuery = currentConnection.createQuery("waeQuery").apply {
//            setCriteria(warnsFilter)
//            setMaxSize(5)
//            setOrder(Order.DESCENDING)
//        }
//        warnsQuery.results.forEach {
//            println(it.message)
//        }
//        warnsView = currentConnection.createView("waeView").apply {
//            criteriaFilter = warnsFilter
//            addListener { logEvent ->
//                log.info(logEvent.message.toString())
//            }
//            activate()
//        }

    }
}