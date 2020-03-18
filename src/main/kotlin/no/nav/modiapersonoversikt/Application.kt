package no.nav.modiapersonoversikt

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.Application")

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)

    val applicationState = ApplicationState()
    val applicationServer = createHttpServer(
            applicationState = applicationState,
            configuration = configuration,
            adminDatasource = dbConfig.adminDataSource(),
            userDataSource = dbConfig.userDataSource()
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(5000, 5000)
    })

    applicationServer.start(wait = true)
}
