package no.nav.modiapersonoversikt

import io.ktor.server.netty.*
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.config.DatabaseConfig
import no.nav.personoversikt.common.ktor.utils.KtorServer

fun runLocally(useMock: Boolean) {
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "http://localhost:8080/azuread/.well-known/openid-configuration")
    System.setProperty("AZURE_APP_CLIENT_ID", "foo")
    System.setProperty("AZURE_APP_CLIENT_SECRET", "bar")

    val db = SpecifiedPostgreSQLContainer()
    db.start()

    val configuration = Configuration(database = DatabaseConfig(jdbcUrl = db.jdbcUrl))
    val dbConfig = DataSourceConfiguration(configuration)

    dbConfig.runFlyway()

    KtorServer.create(Netty, 7070) {
        skrivestotteApp(
            configuration = configuration,
            dataSource = dbConfig.userDataSource(),
            useMock = useMock,
            runLocally = true
        )
    }.start(wait = true)
}

fun main() {
    runLocally(false)
}
