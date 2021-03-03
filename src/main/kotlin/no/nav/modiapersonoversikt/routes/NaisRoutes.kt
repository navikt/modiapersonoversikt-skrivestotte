package no.nav.modiapersonoversikt.routes

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

fun Route.naisRoutes(
    readinessChecks: List<() -> Boolean>,
    livenessChecks: List<() -> Boolean>,
    collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
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
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}
