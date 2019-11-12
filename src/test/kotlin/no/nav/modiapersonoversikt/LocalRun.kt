package no.nav.modiapersonoversikt

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.LocalRun")

fun runLocally(useAuthentication: Boolean) {
    val applicationState = ApplicationState()
    val testDbDataSource = h2DataSource()
    val applicationServer = createHttpServer(
            applicationState,
            7070,
            Configuration(),
            testDbDataSource,
            testDbDataSource,
            useAuthentication
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(1, 1, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}

fun main() {
    runLocally(true)
}