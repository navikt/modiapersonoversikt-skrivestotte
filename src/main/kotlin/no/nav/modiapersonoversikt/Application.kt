package no.nav.modiapersonoversikt

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.infrastructure.Security
import no.nav.modiapersonoversikt.infrastructure.SubjectPrincipal
import no.nav.modiapersonoversikt.infrastructure.exceptionHandler
import no.nav.modiapersonoversikt.infrastructure.notFoundHandler
import no.nav.modiapersonoversikt.skrivestotte.routes.skrivestotteRoutes
import no.nav.modiapersonoversikt.skrivestotte.service.LeaderElectorService
import no.nav.modiapersonoversikt.skrivestotte.storage.JdbcStatisticsProvider
import no.nav.modiapersonoversikt.skrivestotte.storage.JdbcStorageProvider
import no.nav.modiapersonoversikt.utils.JacksonUtils
import no.nav.modiapersonoversikt.utils.measureTimeMillis
import org.slf4j.event.Level
import java.util.*
import javax.sql.DataSource
import kotlin.concurrent.schedule

val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

private const val FEM_MINUTTER: Long = 5 * 60 * 1000
fun Application.skrivestotteApp(
    configuration: Configuration,
    dataSource: DataSource,
    useMock: Boolean = false
) {
    install(XForwardedHeaders)
    install(StatusPages) {
        notFoundHandler()
        exceptionHandler()
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
    }

    val security = Security(
        listOfNotNull(configuration.openam, configuration.azuread)
    )

    install(Authentication) {
        if (useMock) {
            security.setupMock(SubjectPrincipal("Z999999"))
        } else {
            security.setupJWT()
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(JacksonUtils.objectMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        filter { call -> call.request.path().startsWith("/modiapersonoversikt-skrivestotte/skrivestotte") }
        mdc("userId") { security.getSubject(it).joinToString(";") }
    }

    install(MicrometerMetrics) {
        registry = metricsRegistry
    }

    val leaderElectorService = LeaderElectorService(configuration)
    val storageProvider = JdbcStorageProvider(dataSource, configuration)
    val statisticsProvider = JdbcStatisticsProvider(dataSource, configuration)

    Timer().schedule(FEM_MINUTTER, FEM_MINUTTER) {
        if (leaderElectorService.isLeader()) {
            measureTimeMillis("refreshStatistikk") {
                statisticsProvider.refreshStatistikk()
            }
        }
    }

    routing {
        route("modiapersonoversikt-skrivestotte") {
            static {
                resources("webapp")
                defaultResource("index.html", "webapp")
            }

            skrivestotteRoutes(configuration.authproviders, storageProvider, statisticsProvider)
        }
    }
}
