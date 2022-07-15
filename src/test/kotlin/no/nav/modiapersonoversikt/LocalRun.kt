package no.nav.modiapersonoversikt

import io.ktor.server.netty.*
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.config.DatabaseConfig
import no.nav.personoversikt.ktor.utils.KtorServer

fun runLocally(useMock: Boolean) {
    val db = SpecifiedPostgreSQLContainer()
    db.start()

    val configuration = Configuration(database = DatabaseConfig(jdbcUrl = db.jdbcUrl))
    val dbConfig = DataSourceConfiguration(configuration)

    DataSourceConfiguration.migrateDb(configuration, dbConfig.adminDataSource())

    KtorServer.create(Netty, 7070) {
        skrivestotteApp(
            configuration = configuration,
            dataSource = dbConfig.userDataSource(),
            useMock = useMock
        )
    }.start(wait = true)
}

fun main() {
    runLocally(false)
}
