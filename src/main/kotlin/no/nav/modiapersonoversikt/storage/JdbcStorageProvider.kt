package no.nav.modiapersonoversikt.storage

import io.ktor.features.BadRequestException
import kotliquery.*
import no.nav.modiapersonoversikt.XmlLoader
import no.nav.modiapersonoversikt.model.Locale
import no.nav.modiapersonoversikt.model.Tekst
import no.nav.modiapersonoversikt.model.Tekster
import org.slf4j.LoggerFactory
import java.util.*
import javax.sql.DataSource

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.StorageService")

private fun <A> transactional(dataSource: DataSource, operation: (TransactionalSession) -> A): A {
    return using(sessionOf(dataSource)) { session ->
        session.transaction(operation)
    }
}

class JdbcStorageProvider(val dataSource: DataSource) : StorageProvider {
    init {
        transactional(dataSource) { tx ->
            val antallTekster = tx.run(
                    queryOf("SELECT COUNT(*) AS antall FROM TEKST")
                            .map { row -> row.int("antall") }
                            .asSingle
            )

            log.info("Starter JdbcStorageProvider, fant $antallTekster tekster")

            if (antallTekster == 0) {
                log.info("Ingen tekster funnet, laster fra data.xml")
                XmlLoader.get("/data.xml")
                        .forEach { lagreTekst(tx, it) }
            }
        }
    }

    override fun hentTekster(tagFilter: List<String>?): Tekster {
        val tekster = transactional(dataSource) { tx -> hentAlleTekster(tx) }
        return tagFilter
                ?.let { tags ->
                    tekster.filter { it.value.tags.containsAll(tags) }
                }
                ?: tekster

    }

    override fun oppdaterTekst(tekst: Tekst): Tekst {
        if (tekst.id == null) {
            throw BadRequestException("\"id\" må være definert for oppdatering")
        }

        transactional(dataSource) { tx ->
            slettTekst(tx, tekst.id)
            lagreTekst(tx, tekst)
        }

        return tekst
    }

    override fun leggTilTekst(tekst: Tekst): Tekst {
        val id = tekst.id ?: UUID.randomUUID()
        val tekstTilLagring = tekst.copy(id = id)

        transactional(dataSource) { tx ->
            lagreTekst(tx, tekstTilLagring)
        }

        return tekstTilLagring
    }

    override fun slettTekst(id: UUID) = transactional(dataSource) { tx -> slettTekst(tx, id) }

    fun lagreTekst(tx: Session, tekst: Tekst) {
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

    fun slettTekst(tx: Session, id: UUID) {
        tx.run(
                queryOf(
                        "DELETE FROM tekst WHERE id = ?",
                        id.toString()
                ).asUpdate
        )
    }

    private fun hentAlleTekster(tx: Session): Tekster {
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
                    mapOf(*entry
                            .value
                            .map { it.second to it.third }
                            .toTypedArray()
                    )
                }

        return mapOf(
                *tx.run(
                        queryOf("SELECT * FROM tekst order by id")
                                .map { row ->
                                    val id = UUID.fromString(row.string("id"))

                                    id to Tekst(
                                            id,
                                            row.string("overskrift"),
                                            row.string("tags").split("|"),
                                            innhold[row.string("id")] ?: emptyMap()
                                    )
                                }
                                .asList
                ).toTypedArray()
        )
    }
}