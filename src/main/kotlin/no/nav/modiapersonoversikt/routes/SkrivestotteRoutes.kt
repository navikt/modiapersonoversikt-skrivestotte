package no.nav.modiapersonoversikt.routes

import io.ktor.application.call
import io.ktor.auth.AuthenticationRouteSelector
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.modiapersonoversikt.storage.StatisticsProvider
import no.nav.modiapersonoversikt.storage.StorageProvider
import java.util.*

fun Route.conditionalAuthenticate(useAuthentication: Boolean, build: Route.() -> Unit): Route {
    if (useAuthentication) {
        return authenticate(build = build)
    }
    val route = createChild(AuthenticationRouteSelector(listOf<String?>(null)))
    route.build()
    return route
}


fun Route.skrivestotteRoutes(provider: StorageProvider, statistics: StatisticsProvider, useAuthentication: Boolean) {
    route("/skrivestotte") {
        get {
            val tagsFilter = call.request.queryParameters.getAll("tags")
            val enableUsageSort = call.request.queryParameters["usageSort"]?.toBoolean() ?: false
            call.respond(provider.hentTekster(tagsFilter, enableUsageSort))
        }

        conditionalAuthenticate(useAuthentication) {
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

            route("/statistikk") {
                get {
                    call.respond(statistics.hentStatistikk())
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
