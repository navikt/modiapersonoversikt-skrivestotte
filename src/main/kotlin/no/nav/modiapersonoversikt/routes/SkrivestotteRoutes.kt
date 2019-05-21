package no.nav.modiapersonoversikt.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.modiapersonoversikt.storage.StorageProvider
import java.util.*

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

        delete("/{id}") {
            call.parameters["id"]
                    ?.let {
                        provider.slettTekst(UUID.fromString(it))
                        call.respond(HttpStatusCode.OK, "Deleted $it")
                    }
                    ?: call.respond(HttpStatusCode.BadRequest)
        }
    }
}
