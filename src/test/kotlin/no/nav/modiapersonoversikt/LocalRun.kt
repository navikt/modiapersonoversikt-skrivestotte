package no.nav.modiapersonoversikt

import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.infrastructure.HttpServer

fun runLocally(useAuthentication: Boolean) {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)

    DataSourceConfiguration.migrateDb(dbConfig.adminDataSource())

    HttpServer.create("modiapersonoversikt-skrivestotte", 7070) {
        skrivestotteApp(
            configuration = configuration,
            dataSource = dbConfig.userDataSource(),
            useAuthentication = useAuthentication
        )
    }.start(wait = true)
}

fun main() {
    runLocally(true)
}
