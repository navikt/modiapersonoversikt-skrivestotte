package no.nav.modiapersonoversikt

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.JacksonConverter
import io.ktor.metrics.dropwizard.DropwizardMetrics
import io.ktor.request.path
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.dropwizard.DropwizardExports
import no.nav.modiapersonoversikt.JwtUtil.Companion.setupJWT
import no.nav.modiapersonoversikt.JwtUtil.Companion.setupMock
import no.nav.modiapersonoversikt.ObjectMapperProvider.Companion.objectMapper
import no.nav.modiapersonoversikt.routes.naisRoutes
import no.nav.modiapersonoversikt.routes.skrivestotteRoutes
import no.nav.modiapersonoversikt.service.LeaderElectorService
import no.nav.modiapersonoversikt.storage.JdbcStatisticsProvider
import no.nav.modiapersonoversikt.storage.JdbcStorageProvider
import org.slf4j.event.Level
import java.util.*
import javax.sql.DataSource
import kotlin.concurrent.schedule
import no.nav.modiapersonoversikt.JwtUtil.Companion as JwtUtil

private const val FEM_MINUTTER: Long = 5 * 60 * 1000
fun createHttpServer(
    applicationState: ApplicationState,
    port: Int = 7070,
    configuration: Configuration,
    userDataSource: DataSource,
    adminDatasource: DataSource,
    useAuthentication: Boolean = true
): ApplicationEngine = embeddedServer(Netty, port) {

    DataSourceConfiguration.migrateDb(adminDatasource)

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
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/modiapersonoversikt-skrivestotte/skrivestotte") }
        mdc("userId", JwtUtil::getSubject)
    }

    install(DropwizardMetrics) {
        CollectorRegistry.defaultRegistry.register(DropwizardExports(registry))
    }

    val leaderElectorService = LeaderElectorService(configuration)
    val storageProvider = JdbcStorageProvider(userDataSource, configuration)
    val statisticsProvider = JdbcStatisticsProvider(userDataSource, configuration)

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

            naisRoutes(readinessChecks = listOf({ applicationState.initialized }), livenessChecks = listOf({ applicationState.running }, { storageProvider.ping() }))
            skrivestotteRoutes(storageProvider, statisticsProvider)
        }
    }

    applicationState.initialized = true
}
