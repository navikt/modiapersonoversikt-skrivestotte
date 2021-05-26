package no.nav.modiapersonoversikt.routes

import io.ktor.application.call
import io.ktor.auth.AuthenticationRouteSelector
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.modiapersonoversikt.storage.StatisticsProvider
import no.nav.modiapersonoversikt.storage.StorageProvider
import no.nav.modiapersonoversikt.storage.retentionDays
import no.nav.modiapersonoversikt.toLocalDateTime
import java.time.LocalDateTime
import java.util.*

fun Route.skrivestotteRoutes(provider: StorageProvider, statistics: StatisticsProvider) {
    route("/skrivestotte") {
        get {
            val tagsFilter = call.request.queryParameters.getAll("tags")
            val enableUsageSort = call.request.queryParameters["usageSort"]?.toBoolean() ?: false
            call.respond(provider.hentTekster(tagsFilter, enableUsageSort))
        }

        authenticate {
            get("/download") {
                val filename = "skrivestotte-${LocalDateTime.now()}.json"
                call.response.header("Content-Disposition", "attachment; filename=\"$filename\"")
                call.respond(provider.hentTekster(tagFilter = null, sorterBasertPaBruk = false))
            }
            post("/upload") {
                statistics.slettStatistikk()
                call.respond(provider.synkroniserTekster(call.receive()))
            }
          
            put {
                call.respond(provider.oppdaterTekst(call.receive()))
            }

            post {
                call.respond(provider.leggTilTekst(call.receive()))
            }

            delete("/{id}") {
                call.parameters["id"]
                    ?.let {
                        provider.slettTekst(UUID.fromString(it))
                        call.respond(HttpStatusCode.OK, "Deleted $it")
                    }
                    ?: call.respond(HttpStatusCode.BadRequest)
            }
        }

        route("/statistikk") {
            get {
                call.respond(statistics.hentStatistikk())
            }

            get("/overordnetbruk") {
                call.respond(statistics.hentOverordnetBruk())
            }

            get("/detaljertbruk") {
                val now = LocalDateTime.now()
                val from = call.request.queryParameters["from"]
                    ?.toLong()
                    ?.toLocalDateTime()
                    ?: now.minusDays(retentionDays)
                val to = call.request.queryParameters["to"]
                    ?.toLong()
                    ?.toLocalDateTime()
                    ?: now

                call.respond(statistics.hentDetaljertBruk(from, to))
            }

            authenticate {
                get("/refresh") {
                    statistics.refreshStatistikk()
                    call.respond(HttpStatusCode.OK)
                }

                post("/{id}") {
                    call.parameters["id"]
                        ?.let { UUID.fromString(it) }
                        ?.let {
                            statistics.rapporterBruk(it)
                            call.respond(HttpStatusCode.OK)
                        }
                        ?: call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
