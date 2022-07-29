package no.nav.modiapersonoversikt.skrivestotte.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiapersonoversikt.skrivestotte.model.Tekster
import no.nav.modiapersonoversikt.skrivestotte.model.teksterFromJsonMap
import no.nav.modiapersonoversikt.skrivestotte.storage.StatisticsProvider
import no.nav.modiapersonoversikt.skrivestotte.storage.StorageProvider
import no.nav.modiapersonoversikt.skrivestotte.storage.retentionDays
import no.nav.modiapersonoversikt.utils.toLocalDateTime
import java.time.LocalDateTime
import java.util.*

fun Route.skrivestotteRoutes(
    authproviders: Array<out String?>,
    provider: StorageProvider,
    statistics: StatisticsProvider
) {
    route("/skrivestotte") {
        get {
            val tagsFilter = call.request.queryParameters.getAll("tags")
            val enableUsageSort = call.request.queryParameters["usageSort"]?.toBoolean() ?: false
            call.respond(provider.hentTekster(tagsFilter, enableUsageSort))
        }

        authenticate(*authproviders) {
            get("/download") {
                val filename = "skrivestotte-${LocalDateTime.now()}.json"
                call.response.header("Content-Disposition", "attachment; filename=\"$filename\"")
                call.respond(provider.hentTekster(tagFilter = null, sorterBasertPaBruk = false))
            }
            post("/upload") {
                statistics.slettAllStatistikk()
                val tekster: Tekster = teksterFromJsonMap(call.receive())
                call.respond(provider.synkroniserTekster(tekster))
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

            authenticate(*authproviders) {
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