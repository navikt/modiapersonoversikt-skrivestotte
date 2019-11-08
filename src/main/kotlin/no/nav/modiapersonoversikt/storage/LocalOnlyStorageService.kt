package no.nav.modiapersonoversikt.storage

import no.nav.modiapersonoversikt.XmlLoader
import no.nav.modiapersonoversikt.model.Tekst
import no.nav.modiapersonoversikt.model.Tekster
import java.util.*

class LocalOnlyStorageService : StorageProvider {
    val tekster: Tekster = emptyMap<UUID, Tekst>().plus(XmlLoader.get("/data.xml")
            .map { it.id!! to it }
    )

    override fun hentTekster(tagFilter: List<String>?): Tekster {
        return tagFilter
                ?.let {
                    tags -> tekster.filter { it.value.tags.containsAll(tags) }
                }
                ?: tekster
    }

    override fun oppdaterTekst(tekst: Tekst): Tekst {
        TODO("editing not allowed while in localonly mode")
    }

    override fun leggTilTekst(tekst: Tekst): Tekst {
        TODO("editing not allowed while in localonly mode")
    }

    override fun slettTekst(id: UUID) {
        TODO("editing not allowed while in localonly mode")
    }

}