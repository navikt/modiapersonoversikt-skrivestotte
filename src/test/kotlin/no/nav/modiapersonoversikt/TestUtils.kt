package no.nav.modiapersonoversikt

import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.config.DatabaseConfig
import no.nav.modiapersonoversikt.skrivestotte.storage.transactional
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

class SpecifiedPostgreSQLContainer : PostgreSQLContainer<SpecifiedPostgreSQLContainer>("postgres:14.3-alpine")

interface WithDatabase {
    companion object {
        private val postgreSQLContainer = SpecifiedPostgreSQLContainer().apply { start() }
        private val configuration = Configuration(
            database = DatabaseConfig(jdbcUrl = postgreSQLContainer.jdbcUrl)
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

fun <R> withTestApp(jdbcUrl: String? = null, test: suspend ApplicationTestBuilder.() -> R) {
    val dataAwareApp = fun Application.() {
        if (jdbcUrl != null) {
            val config = Configuration(database = DatabaseConfig(jdbcUrl = jdbcUrl))
            val dbConfig = DataSourceConfiguration(config)
            skrivestotteApp(config, dbConfig.userDataSource(), useMock = true, runLocally = true)
        }
    }

    val moduleFunction: Application.() -> Unit = {
        dataAwareApp()
    }

    return testApplication {
        application(moduleFunction)
        test()
    }
}

fun configureMockserver(block: MockWebServer.() -> Unit): MockWebServer {
    return MockWebServer().apply(block)
}
fun MockWebServer.run(block: MockWebServer.() -> Unit) {
    this.start()
    this.apply(block)
    this.shutdown()
}