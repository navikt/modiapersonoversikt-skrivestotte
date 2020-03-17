package no.nav.modiapersonoversikt

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.LocalRun")

fun runLocally(useAuthentication: Boolean) {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)
    val applicationState = ApplicationState()

    val applicationServer = createHttpServer(
            applicationState = applicationState,
            port = 7070,
            configuration = Configuration(),
            adminDatasource = dbConfig.adminDataSource(),
            userDataSource = dbConfig.userDataSource(),
            useAuthentication = useAuthentication
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(1000, 1000)
    })

    applicationServer.start(wait = true)
}

fun main() {
    runLocally(true)
}
