package no.nav.modiapersonoversikt.storage

import no.nav.modiapersonoversikt.model.Tekst
import no.nav.modiapersonoversikt.model.Tekster
import java.util.*

interface StorageProvider {
    fun hentTekster(): Tekster
    fun oppdaterTekst(tekst: Tekst): Tekst
    fun leggTilTekst(tekst: Tekst): Tekst
    fun slettTekst(id: UUID)
}
