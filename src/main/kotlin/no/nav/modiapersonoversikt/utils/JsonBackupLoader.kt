package no.nav.modiapersonoversikt.utils

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.modiapersonoversikt.skrivestotte.model.Tekster
import no.nav.modiapersonoversikt.skrivestotte.model.teksterFromJsonMap
import java.util.*

object JsonBackupLoader {
    private val tekster: Tekster = getTekster()
    private val vekttall: Map<String, Int> = tekster
            .mapKeys { it.key.toString() }
            .mapValues { it.value.vekttall }

    fun getTekster(): Tekster {
        val inputStream = this::class.java.getResourceAsStream("/skrivestotte-2021-05-28T13_48_35.477.json")
        val rawData = requireNotNull(inputStream).use {
            ObjectMapperProvider.objectMapper.readValue<Map<String, Map<String, Any>>>(it)
        }
        return teksterFromJsonMap(rawData)
    }

    fun getVekttall(id: String): Int = vekttall.getOrDefault(id, 0)
    fun getVekttall(id: UUID): Int = getVekttall(id.toString())
}