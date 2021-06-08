package no.nav.modiapersonoversikt

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.metrics.dropwizard.*
import io.ktor.request.*
import io.ktor.routing.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.dropwizard.DropwizardExports
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.infrastructure.Security
import no.nav.modiapersonoversikt.infrastructure.Security.Companion.setupJWT
import no.nav.modiapersonoversikt.infrastructure.Security.Companion.setupMock
import no.nav.modiapersonoversikt.infrastructure.SubjectPrincipal
import no.nav.modiapersonoversikt.infrastructure.exceptionHandler
import no.nav.modiapersonoversikt.infrastructure.notFoundHandler
import no.nav.modiapersonoversikt.skrivestotte.routes.skrivestotteRoutes
import no.nav.modiapersonoversikt.skrivestotte.service.LeaderElectorService
import no.nav.modiapersonoversikt.skrivestotte.storage.JdbcStatisticsProvider
import no.nav.modiapersonoversikt.skrivestotte.storage.JdbcStorageProvider
import no.nav.modiapersonoversikt.utils.ObjectMapperProvider
import no.nav.modiapersonoversikt.utils.measureTimeMillis
import org.slf4j.event.Level
import java.util.*
import javax.sql.DataSource
import kotlin.concurrent.schedule

private const val FEM_MINUTTER: Long = 5 * 60 * 1000
fun Application.skrivestotteApp(
    configuration: Configuration,
    dataSource: DataSource,
    useAuthentication: Boolean = true
) {
    install(StatusPages) {
        notFoundHandler()
        exceptionHandler()
    }

    install(CORS) {
        anyHost()
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        allowCredentials = true
    }

    install(Authentication) {
        if (useAuthentication) {
            setupJWT(configuration.jwksUrl, configuration.jwtIssuer)
        } else {
            setupMock(SubjectPrincipal("Z999999"))
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(ObjectMapperProvider.objectMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/modiapersonoversikt-skrivestotte/skrivestotte") }
        mdc("userId", Security.Companion::getSubject)
    }

    install(DropwizardMetrics) {
        CollectorRegistry.defaultRegistry.register(DropwizardExports(registry))
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

            skrivestotteRoutes(storageProvider, statisticsProvider)
        }
    }
}