package no.nav.modiapersonoversikt

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.metrics.dropwizard.DropwizardMetrics
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.dropwizard.DropwizardExports
import no.nav.modiapersonoversikt.routes.naisRoutes
import no.nav.modiapersonoversikt.routes.skrivestotteRoutes
import no.nav.modiapersonoversikt.storage.StorageProvider
import org.slf4j.event.Level

fun createHttpServer(applicationState: ApplicationState,
                     provider: StorageProvider,
                     port: Int = 7070,
                     configuration: Configuration,
                     useAuthentication: Boolean = true): ApplicationEngine = embeddedServer(Netty, port) {

    install(StatusPages) {
        notFoundHandler()
        exceptionHandler()
    }

    if (useAuthentication) {
        install(Authentication) {
            jwt {
                authHeader(useJwtFromCookie)
                realm = "modiapersonoversikt-skrivestøtte"
                verifier(configuration.jwksUrl, configuration.jwtIssuer)
                validate { validateJWT(it) }
            }
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/skrivestotte") }
        mdc("userId", getSubjectFromApplicationCall)
    }

    install(DropwizardMetrics) {
        CollectorRegistry.defaultRegistry.register(DropwizardExports(registry))
    }

    routing {
        naisRoutes(readinessCheck = { applicationState.initialized }, livenessCheck = { applicationState.running })
        skrivestotteRoutes(provider, useAuthentication)
    }

    applicationState.initialized = true
}