package no.nav.modiapersonoversikt.skrivestotte.storage

import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import kotliquery.Session
import kotliquery.queryOf
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.log
import no.nav.modiapersonoversikt.skrivestotte.model.Locale
import no.nav.modiapersonoversikt.skrivestotte.model.Tekst
import no.nav.modiapersonoversikt.skrivestotte.model.Tekster
import no.nav.modiapersonoversikt.utils.JsonBackupLoader
import no.nav.modiapersonoversikt.utils.measureTimeMillisSuspended
import no.nav.personoversikt.common.utils.SelftestGenerator
import java.util.*
import javax.sql.DataSource
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.seconds

class JdbcStorageProvider(private val dataSource: DataSource, private val configuration: Configuration) : StorageProvider {
    private val selftest = SelftestGenerator.Reporter("Database", true)

    init {
        fixedRateTimer("Database check", daemon = true, initialDelay = 0, period = 10.seconds.inWholeMilliseconds) {
            runBlocking {
                selftest.ping { kanKobleTilDatabasen() }
            }
        }

        runBlocking {
            transactional(dataSource) { tx ->
                val antallTekster = tx.run(
                    queryOf("SELECT COUNT(*) AS antall FROM TEKST")
                        .map { row -> row.int("antall") }
                        .asSingle
                )

                log.info("Starter JdbcStorageProvider, fant $antallTekster tekster")

                if (antallTekster == 0) {
                    log.info("Ingen tekster funnet, laster fra json-backup")
                    val tekster = JsonBackupLoader.getTekster()
                    tekster
                        .values
                        .forEach { lagreTekst(tx, it) }
                    log.info("Prepopulert databasen med ${tekster.size} tekster")
                }
            }
        }
    }

    override suspend fun hentTekster(tagFilter: List<String>?, sorterBasertPaBruk: Boolean): Tekster {
        val tekster = transactional(dataSource) { tx -> hentAlleTekster(tx, sorterBasertPaBruk) }
        return tagFilter
            ?.let { tags ->
                tekster.filter { it.value.tags.containsAll(tags) }
            }
            ?: tekster
    }

    override suspend fun oppdaterTekst(tekst: Tekst): Tekst {
        if (tekst.id == null) {
            throw BadRequestException("\"id\" må være definert for oppdatering")
        }

        transactional(dataSource) { tx ->
            slettTekst(tx, tekst.id)
            lagreTekst(tx, tekst)
        }

        return tekst
    }

    override suspend fun leggTilTekst(tekst: Tekst): Tekst {
        val id = tekst.id ?: UUID.randomUUID()
        val tekstTilLagring = tekst.copy(id = id)

        transactional(dataSource) { tx ->
            lagreTekst(tx, tekstTilLagring)
        }

        return tekstTilLagring
    }

    override suspend fun slettTekst(id: UUID) = transactional(dataSource) { tx -> slettTekst(tx, id) }

    override suspend fun synkroniserTekster(tekster: Tekster): Tekster {
        return measureTimeMillisSuspended("synkroniserTekster") {
            transactional(dataSource) { tx ->
                log.info("Starter synkronisering av ${tekster.size} tekster")
                slettAlleTekster(tx)
                log.info("Alle eksisterende tekster slettet")
                tekster.values.forEach { tekst ->
                    log.info("Lagrer ny tekst ${tekst.id}")
                    lagreTekst(tx, tekst)
                }
                log.info("Henter alle nye tekster")
                hentAlleTekster(tx, false)
            }
        }
    }

    private suspend fun kanKobleTilDatabasen() {
        transactional(dataSource) { tx ->
            tx.run(
                queryOf("select 'ok' as status")
                    .map { row -> row.string("status") }
                    .asSingle
            )
        }
    }

    internal fun lagreTekst(tx: Session, tekst: Tekst) {
        tx.run(
            queryOf(
                "INSERT INTO tekst (id, overskrift, tags) VALUES (:id, :overskrift, :tags)",
                mapOf(
                    "id" to tekst.id.toString(),
                    "overskrift" to tekst.overskrift,
                    "tags" to tekst.tags.joinToString("|")
                )
            ).asUpdate
        )

        tekst.innhold.forEach { (locale, innhold) ->
            tx.run(
                queryOf(
                    "INSERT INTO innhold (tekst_id, locale, innhold) VALUES (:id, :locale, :innhold)",
                    mapOf(
                        "id" to tekst.id.toString(),
                        "locale" to locale.name,
                        "innhold" to innhold
                    )
                ).asUpdate
            )
        }
    }

    private fun slettTekst(tx: Session, id: UUID) {
        tx.run(
            queryOf(
                "DELETE FROM tekst WHERE id = ?",
                id.toString()
            ).asUpdate
        )
    }

    private fun slettAlleTekster(tx: Session) {
        tx.run(queryOf("DELETE FROM innhold").asUpdate)
        tx.run(queryOf("DELETE FROM tekst").asUpdate)
    }

    private fun hentAlleTekster(tx: Session, sorterBasertPaBruk: Boolean): Tekster {
        val innhold = tx.run(
            queryOf("SELECT * FROM innhold")
                .map { row ->
                    Triple(
                        row.string("tekst_id"),
                        Locale.valueOf(row.string("locale")),
                        row.string("innhold")
                    )
                }.asList
        )
            .groupBy { it.first }
            .mapValues { entry ->
                mapOf(
                    *entry
                        .value
                        .map { it.second to it.third }
                        .toTypedArray()
                )
            }

        val hentAlleQuery = when (configuration.useStatisticsSort || sorterBasertPaBruk) {
            true -> queryOf(
                """
                        SELECT * FROM tekst t
                        LEFT JOIN statistikk s ON (t.id = s.id)
                        ORDER BY s.brukt DESC NULLS LAST, t.overskrift
                """.trimIndent()
            )
            false -> queryOf(
                """
                SELECT * FROM tekst t
                LEFT JOIN statistikk s ON (t.id = s.id) 
                ORDER BY t.id
                """.trimIndent()
            )
        }

        return mapOf(
            *tx.run(
                hentAlleQuery.map { row ->
                    val id = UUID.fromString(row.string("id"))

                    id to Tekst(
                        id,
                        row.string("overskrift"),
                        row.string("tags").split("|"),
                        innhold[row.string("id")] ?: emptyMap(),
                        row.intOrNull("brukt") ?: 0
                    )
                }
                    .asList
            )
                .toTypedArray()
        )
    }
}
