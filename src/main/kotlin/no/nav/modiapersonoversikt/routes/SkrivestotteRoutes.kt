package no.nav.modiapersonoversikt.routes

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.modiapersonoversikt.storage.StorageProvider

fun Route.skrivestotteRoutes(provider: StorageProvider) {
    route("/skrivestotte") {
        get {
            call.respond(provider.hentTekster())
        }

        put {
            call.respond(provider.oppdaterTekst(call.receive()))
        }

        post {
            call.respond(provider.leggTilTekst(call.receive()))
        }

        delete {
            call.respond(provider.slettTekst(call.receive()))
        }
    }
}
