package no.nav.modiapersonoversikt.storage

import no.nav.modiapersonoversikt.model.Tekst
import no.nav.modiapersonoversikt.model.Tekster
import java.util.*

interface StorageProvider {
    suspend fun hentTekster(tagFilter: List<String>?, sorterBasertPaBruk: Boolean): Tekster
    suspend fun oppdaterTekst(tekst: Tekst): Tekst
    suspend fun leggTilTekst(tekst: Tekst): Tekst
    suspend fun slettTekst(id: UUID)
    suspend fun synkroniserTekster(tekster: Tekster): Tekster
}
