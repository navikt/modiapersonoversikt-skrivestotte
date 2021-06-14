package no.nav.modiapersonoversikt.skrivestotte.storage

import no.nav.modiapersonoversikt.skrivestotte.model.Tekst
import no.nav.modiapersonoversikt.skrivestotte.model.Tekster
import java.util.*

interface StorageProvider {
    suspend fun hentTekster(tagFilter: List<String>?, sorterBasertPaBruk: Boolean): Tekster
    suspend fun oppdaterTekst(tekst: Tekst): Tekst
    suspend fun leggTilTekst(tekst: Tekst): Tekst
    suspend fun slettTekst(id: UUID)
    suspend fun synkroniserTekster(tekster: Tekster): Tekster
}
