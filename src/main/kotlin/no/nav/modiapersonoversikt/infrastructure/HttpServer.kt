package no.nav.modiapersonoversikt.infrastructure

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.modiapersonoversikt.log

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)
object HttpServer {
    fun create(appname: String, port: Int, module: Application.() -> Unit): ApplicationEngine {
        val applicationState = ApplicationState()

        val applicationServer = embeddedServer(Netty, port) {
            naisApplication(appname, applicationState, module)
        }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                log.info("Shutdown hook called, shutting down gracefully")
                applicationState.initialized = false
                applicationServer.stop(5000, 5000)
            }
        )

        return applicationServer
    }
}

fun Application.naisApplication(appname: String, applicationState: ApplicationState, module: Application.() -> Unit) {
    routing {
        route(appname) {
            naisRoutes(
                readinessChecks = listOf { applicationState.initialized },
                livenessChecks = listOf { applicationState.running }
            )
        }
    }
    module(this)
    applicationState.initialized = true
}
