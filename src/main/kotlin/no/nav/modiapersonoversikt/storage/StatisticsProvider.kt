package no.nav.modiapersonoversikt.storage

import java.util.*

interface StatisticsProvider {
    fun hentStatistikk(): Map<UUID, Int>
    fun rapporterBruk(id: UUID): Int
    fun refreshStatistikk()
}
