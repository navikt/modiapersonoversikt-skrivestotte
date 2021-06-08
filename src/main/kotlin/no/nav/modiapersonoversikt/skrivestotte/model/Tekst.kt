package no.nav.modiapersonoversikt.skrivestotte.model

import java.util.*

typealias Tekster = Map<UUID, Tekst>
fun teksterFromJsonMap(data: Map<String, Map<String, Any>>): Tekster {
    return data
        .mapValues { Tekst.from(it.value) }
        .mapKeys { requireNotNull(it.value.id) }
}

enum class Locale {
    nb_NO, nn_NO, en_US, se_NO, de_DE, fr_FR, es_ES, pl_PL, ru_RU, ur
}

data class Tekst(
    val id: UUID?,
    val overskrift: String,
    val tags: List<String>,
    val innhold: Map<Locale, String>,
    val vekttall: Int = 0
) {
    companion object {
        fun from(map: Map<String, Any>): Tekst {
            val id = UUID.fromString(map["id"].toString())
            val tagsRaw = map["tags"]
            val innholdRaw = map["innhold"]

            return Tekst(
                id = UUID.fromString(map["id"].toString()),
                overskrift = map["overskrift"].toString(),
                tags = when (tagsRaw) {
                    is List<*> -> tagsRaw.map { it.toString() }
                    else -> throw IllegalStateException("Tag liste for $id innehold ugyldig data: $tagsRaw")
                },
                innhold = when (innholdRaw) {
                    is Map<*, *> -> innholdRaw
                        .mapKeys { Locale.valueOf(it.key.toString()) }
                        .mapValues { it.value.toString() }
                    else -> throw IllegalStateException("Innhold map for $id innehold ugyldig data: $innholdRaw")
                },
                vekttall = map["vekttall"].toString().toInt()
            )
        }
    }
}