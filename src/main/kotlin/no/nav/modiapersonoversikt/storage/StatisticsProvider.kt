package no.nav.modiapersonoversikt.storage

import java.time.LocalDateTime
import java.util.*

interface StatisticsProvider {
    suspend fun hentStatistikk(): Map<UUID, Int>
    suspend fun rapporterBruk(id: UUID): Int
    suspend fun refreshStatistikk()
    suspend fun hentOverordnetBruk(): List<StatistikkEntry>
    suspend fun hentDetaljertBruk(from: LocalDateTime, to: LocalDateTime): List<DetaljertStatistikk>
    suspend fun slettAllStatistikk()
}
