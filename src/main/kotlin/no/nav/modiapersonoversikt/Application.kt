package no.nav.modiapersonoversikt

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.infrastructure.*
import no.nav.modiapersonoversikt.skrivestotte.routes.skrivestotteRoutes
import no.nav.modiapersonoversikt.skrivestotte.service.LeaderElectorService
import no.nav.modiapersonoversikt.skrivestotte.storage.JdbcStatisticsProvider
import no.nav.modiapersonoversikt.skrivestotte.storage.JdbcStorageProvider
import no.nav.modiapersonoversikt.utils.JacksonUtils
import no.nav.modiapersonoversikt.utils.measureTimeMillis
import no.nav.personoversikt.ktor.utils.Metrics
import no.nav.personoversikt.ktor.utils.Security
import no.nav.personoversikt.ktor.utils.Selftest
import org.slf4j.event.Level
import javax.sql.DataSource
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes

fun Application.skrivestotteApp(
    configuration: Configuration,
    dataSource: DataSource,
    useMock: Boolean = false,
    runLocally: Boolean = false
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

    install(Metrics.Plugin) {
        contextpath = appContextpath
    }

    install(Selftest.Plugin) {
        appname = appName
        contextpath = appContextpath
        version = appImage
    }

    val authproviders = setupSecurity(configuration, useMock, runLocally)

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(JacksonUtils.objectMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        filter { call -> call.request.path().startsWith("/modiapersonoversikt-skrivestotte/skrivestotte") }
        mdc("userId") {
            it.principal<Security.SubjectPrincipal>()?.subject ?: "Unauthenticated"
        }
    }

    val leaderElectorService = LeaderElectorService(configuration)
    val storageProvider = JdbcStorageProvider(dataSource, configuration)
    val statisticsProvider = JdbcStatisticsProvider(dataSource, configuration)

    fixedRateTimer(
        daemon = true,
        initialDelay = 5.minutes.inWholeMilliseconds,
        period = 5.minutes.inWholeMilliseconds
    ) {
        if (leaderElectorService.isLeader()) {
            measureTimeMillis("refreshStatistikk") {
                statisticsProvider.refreshStatistikk()
            }
        }
    }

    routing {
        route(appContextpath) {
            authenticate(*authproviders) {
                static {
                    resources("webapp")
                    defaultResource("index.html", "webapp")
                }
            }

            skrivestotteRoutes(authproviders, storageProvider, statisticsProvider)
        }
    }
}