package no.nav.modiapersonoversikt

import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.infrastructure.HttpServer

fun runLocally(useMock: Boolean) {
    val db = SpecifiedPostgreSQLContainer()
    db.start()

    val configuration = Configuration(jdbcUrl = db.jdbcUrl)
    val dbConfig = DataSourceConfiguration(configuration)

    DataSourceConfiguration.migrateDb(configuration, dbConfig.adminDataSource())

    HttpServer.create("modiapersonoversikt-skrivestotte", 7070) {
        skrivestotteApp(
            configuration = configuration,
            dataSource = dbConfig.userDataSource(),
            useMock = useMock
        )
    }.start(wait = true)
}

fun main() {
    runLocally(true)
}
