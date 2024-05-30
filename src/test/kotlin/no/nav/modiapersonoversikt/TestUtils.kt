package no.nav.modiapersonoversikt

import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.config.DataSourceConfiguration
import no.nav.modiapersonoversikt.config.DatabaseConfig
import no.nav.modiapersonoversikt.skrivestotte.storage.transactional
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.intellij.lang.annotations.Language
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
            dbConfig.runFlyway()
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

fun withExternalDependencies(block: () -> Unit) {
    @Language("json")
    val oidcWellKnownResponse = """
        {
            "jwks_uri": "",
            "issuer": "",
            "authorization_endpoint": "",
            "token_endpoint": ""
        }
    """.trimIndent()

    val mockResponse = MockResponse()
        .setBody(oidcWellKnownResponse)
        .setHeader("Content-Type", "application/json")

    configureMockserver { mockResponse }.run {
        withProperty("AZURE_APP_WELL_KNOWN_URL", url("").toString()) {
            block()
        }
    }
}

fun configureMockserver(block: RecordedRequest.() -> MockResponse): MockWebServer {
    val server = MockWebServer()
    server.dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return block(request)
        }
    }
    return server
}
fun MockWebServer.run(block: MockWebServer.() -> Unit) {
    this.start()
    this.apply(block)
    this.shutdown()
}

fun <T> withProperty(key: String, value: String?, block: () -> T): T {
    val oldValue: String? = System.getProperty(key)
    setNullableProperty(key, value)
    val result: T = block()
    setNullableProperty(key, oldValue)
    return result
}

private fun setNullableProperty(key: String, value: String?) {
    if (value == null) {
        System.clearProperty(key)
    } else {
        System.setProperty(key, value)
    }
}