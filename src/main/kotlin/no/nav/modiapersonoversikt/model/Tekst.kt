package no.nav.modiapersonoversikt.model

import java.util.*

typealias Tekster = Map<UUID, Tekst>

enum class Locale {
    nb_NO, nn_NO, en_US
}

data class Tekst(
        val id: UUID?,
        val overskrift: String,
        val tags: List<String>,
        val innhold: Map<Locale, String>
)