package no.nav.modiapersonoversikt.model

import java.util.*

typealias Tekster = Map<UUID, Tekst>

enum class Locale {
    nb_NO, nn_NO, en_US, se_NO, de_DE, fr_FR, es_ES, pl_PL, ru_RU, ur
}

data class Tekst(
        val id: UUID?,
        val overskrift: String,
        val tags: List<String>,
        val innhold: Map<Locale, String>
)