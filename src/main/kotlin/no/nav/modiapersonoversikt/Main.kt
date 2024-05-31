package no.nav.modiapersonoversikt

import io.ktor.server.netty.*
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.personoversikt.common.ktor.utils.KtorServer
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.Application")

fun main() {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)
    dbConfig.runFlyway()

    KtorServer.create(Netty, 7070) {
        skrivestotteApp(
            configuration = configuration,
            dataSource = dbConfig.userDataSource()
        )
    }.start(wait = true)
}
