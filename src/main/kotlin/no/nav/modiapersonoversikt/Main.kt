package no.nav.modiapersonoversikt

import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.infrastructure.HttpServer
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.Application")

fun main() {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)
    DataSourceConfiguration.migrateDb(dbConfig.adminDataSource())

    HttpServer.create("modiapersonoversikt-skrivestotte", 7070) {
        skrivestotteApp(
            configuration = configuration,
            dataSource = dbConfig.userDataSource()
        )
    }.start(wait = true)
}