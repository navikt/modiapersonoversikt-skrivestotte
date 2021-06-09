package no.nav.modiapersonoversikt.skrivestotte.storage

import kotlinx.coroutines.runBlocking
import kotliquery.Session
import kotliquery.queryOf
import no.nav.modiapersonoversikt.WithDatabase
import no.nav.modiapersonoversikt.skrivestotte.model.Locale
import no.nav.modiapersonoversikt.skrivestotte.model.Tekst
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.*

internal class JdbcStatisticsProviderTest : WithDatabase {
    val dataSource = dataSource()
    val dao: StatisticsProvider = JdbcStatisticsProvider(dataSource, configuration())
    val tekstDao = JdbcStorageProvider(dataSource, configuration())

    val ID1 = UUID.randomUUID()
    val ID2 = UUID.randomUUID()
    val ID3 = UUID.randomUUID()

    @Test
    fun `hent statistikk skal hente ut all statistikk som finnes i databasen`() = runBlocking {
        transactional(dataSource) { tx ->
            listOf(ID1, ID2, ID3).forEach { insertTekst(tx, it) }
            insertRawStatistikk(tx, ID1, LocalDateTime.now())
            insertRawStatistikk(tx, ID2, LocalDateTime.now().minusDays(1))
            insertRawStatistikk(tx, ID2, LocalDateTime.now().minusDays(2))
            insertRawStatistikk(tx, ID3, LocalDateTime.now().minusDays(61))
        }

        val detaljertStatistikk = dao.hentDetaljertBruk(
            from = LocalDateTime.now().minusDays(120),
            to = LocalDateTime.now()
        )

        assertEquals(3, detaljertStatistikk.size)
        assertEquals(1, detaljertStatistikk.find { it.id == ID1 }?.vekttall)
        assertEquals(2, detaljertStatistikk.find { it.id == ID2 }?.vekttall)
        assertEquals(1, detaljertStatistikk.find { it.id == ID3 }?.vekttall)
    }

    @Test
    fun `rapporterBruk skal legge til nytt raw innslag`() = runBlocking {
        transactional(dataSource) { tx ->
            insertTekst(tx, ID1)
        }

        repeat(3) {
            dao.rapporterBruk(ID1)
        }

        val detaljertStatistikk = dao.hentDetaljertBruk(
            from = LocalDateTime.now().minusDays(120),
            to = LocalDateTime.now()
        )

        assertEquals(3, detaljertStatistikk.find { it.id == ID1 }?.vekttall)
    }

    @Test
    fun `refreshing statistikk skal slette gamle innslag og oppdatere hovedtabellen`() = runBlocking {
        val hovedtabellStart = dao.hentStatistikk()
        assertEquals(0, hovedtabellStart.size)

        transactional(dataSource) { tx ->
            listOf(ID1, ID2, ID3).forEach { insertTekst(tx, it) }
            insertRawStatistikk(tx, ID1, LocalDateTime.now())
            insertRawStatistikk(tx, ID2, LocalDateTime.now().minusDays(1))
            insertRawStatistikk(tx, ID2, LocalDateTime.now().minusDays(2))
            insertRawStatistikk(tx, ID3, LocalDateTime.now().minusDays(61))
        }

        dao.refreshStatistikk()
        val hovedtabellEnd = dao.hentStatistikk()
        val detaljertStatistikk = dao.hentDetaljertBruk(
            from = LocalDateTime.now().minusDays(120),
            to = LocalDateTime.now()
        )

        assertEquals(2, hovedtabellEnd.size)
        assertTrue(hovedtabellEnd.containsKey(ID1))
        assertTrue(hovedtabellEnd.containsKey(ID2))
        assertFalse(hovedtabellEnd.containsKey(ID3))

        assertEquals(2, detaljertStatistikk.size, "bevare raw-innslag nyere enn 60 dager")
        assertNull(detaljertStatistikk.find { it.id == ID3 }, "eldre enn 60 dager slettes")
    }

    private fun insertTekst(tx: Session, id: UUID) {
        tekstDao.lagreTekst(
            tx,
            Tekst(
                id = id,
                overskrift = "Overskrift $id",
                tags = emptyList(),
                innhold = mapOf(
                    Locale.nb_NO to "Innhold"
                )
            )
        )
    }

    private fun insertRawStatistikk(tx: Session, id: UUID, time: LocalDateTime) {
        tx.run(
            queryOf(
                "INSERT INTO statistikk_raw (tidspunkt, tekstid) VALUES (:time, :id)",
                mapOf(
                    "time" to time,
                    "id" to id
                )
            ).asUpdate
        )
    }
}
