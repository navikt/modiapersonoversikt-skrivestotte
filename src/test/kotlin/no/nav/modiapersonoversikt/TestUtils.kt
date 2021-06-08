package no.nav.modiapersonoversikt

import io.ktor.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.infrastructure.ApplicationState
import no.nav.modiapersonoversikt.infrastructure.naisApplication
import no.nav.modiapersonoversikt.skrivestotte.storage.transactional
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

class SpecifiedPostgreSQLContainer : PostgreSQLContainer<SpecifiedPostgreSQLContainer>("postgres:9.6.12")

interface WithDatabase {
    companion object {
        private val postgreSQLContainer = SpecifiedPostgreSQLContainer().apply { start() }
        private val configuration = Configuration(
            jdbcUrl = postgreSQLContainer.jdbcUrl
        )
        private val dbConfig = DataSourceConfiguration(configuration)

        @BeforeAll
        @JvmStatic
        fun setupDatabase() {
            DataSourceConfiguration.migrateDb(configuration, dbConfig.adminDataSource())
        }
    }

    @BeforeEach
    fun clearDatabase() {
        runBlocking {
            transactional(dbConfig.adminDataSource()) { tx ->
                tx.run(queryOf("DELETE FROM innhold").asExecute)
                tx.run(queryOf("DELETE FROM tekst").asExecute)
                tx.run(queryOf("DELETE FROM statistikk_raw").asExecute)
                tx.run(queryOf("DELETE FROM statistikk").asExecute)
            }
        }
    }

    fun configuration(): Configuration = configuration
    fun dataSource(): DataSource = dbConfig.userDataSource()
    fun connectionUrl(): String = postgreSQLContainer.jdbcUrl
}

fun <R> withTestApp(jdbcUrl: String? = null, test: TestApplicationEngine.() -> R): R {
    val dataAwareApp = fun Application.() {
        if (jdbcUrl != null) {
            val config = Configuration(jdbcUrl = jdbcUrl)
            val dbConfig = DataSourceConfiguration(config)
            skrivestotteApp(config, dbConfig.userDataSource(), false)
        }
    }

    val moduleFunction: Application.() -> Unit = {
        naisApplication("modiapersonoversikt-draft", ApplicationState()) {}
        dataAwareApp()
    }

    return withTestApplication(moduleFunction, test)
}
