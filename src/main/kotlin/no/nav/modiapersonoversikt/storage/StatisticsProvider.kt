package no.nav.modiapersonoversikt.storage

import java.time.LocalDateTime
import java.util.*

interface StatisticsProvider {
    fun hentStatistikk(): Map<UUID, Int>
    fun rapporterBruk(id: UUID): Int
    fun refreshStatistikk()
    fun hentOverordnetBruk(): List<StatistikkEntry>
    fun hentDetaljertBruk(from: LocalDateTime, to: LocalDateTime): List<DetaljertStatistikk>
}
