package no.nav.modiapersonoversikt.infrastructure

import io.ktor.server.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiapersonoversikt.metricsRegistry

fun Route.naisRoutes(
    readinessChecks: List<() -> Boolean>,
    livenessChecks: List<() -> Boolean>
) {

    get("/isAlive") {
        if (livenessChecks.all { it() }) {
            call.respondText("Alive")
        } else {
            call.respondText("Not alive", status = HttpStatusCode.InternalServerError)
        }
    }

    get("/isReady") {
        if (readinessChecks.all { it() }) {
            call.respondText("Ready")
        } else {
            call.respondText("Not ready", status = HttpStatusCode.InternalServerError)
        }
    }

    get("/metrics") {
        call.respondText(metricsRegistry.scrape())
    }
}
