package no.nav.modiapersonoversikt.storage

import kotliquery.Session
import kotliquery.queryOf
import java.util.*
import javax.sql.DataSource

const val table = "statistikk"

class JdbcStatisticsProvider(val dataSource: DataSource) : StatisticsProvider {
    override fun hentStatistikk(): Map<UUID, Int> {
        return transactional(dataSource) { tx ->
            val data = tx.run(queryOf("SELECT * FROM $table")
                    .map { row ->
                        UUID.fromString(row.string("id")) to row.int("brukt")
                    }
                    .asList
            )
            mapOf(*(data.toTypedArray()))
        }
    }

    override fun rapporterBruk(id: UUID): Int {
        return transactional(dataSource) { tx ->
            val data = hentStatistikk(tx, id)
            lagreStatistikk(tx, id, data + 1)
        }
    }

    private fun hentStatistikk(tx: Session, id: UUID): Int {
        val lagretVerdi: Int? = tx.run(
                queryOf(
                        "SELECT * FROM $table WHERE id = :id",
                        mapOf("id" to id.toString())
                )
                        .map { row -> row.int("brukt") }
                        .asSingle
        )

        if (lagretVerdi == null) {
            tx.run(
                    queryOf(
                            "INSERT INTO $table (id, brukt) VALUES (:id, :brukt)",
                            mapOf(
                                    "id" to id.toString(),
                                    "brukt" to 0
                            )
                    ).asUpdate
            )
            return 0
        }

        return lagretVerdi
    }

    private fun lagreStatistikk(tx: Session, id: UUID, antall: Int): Int {
        tx.run(
                queryOf(
                        "UPDATE $table SET brukt = :brukt WHERE id = :id",
                        mapOf(
                                "id" to id.toString(),
                                "brukt" to antall
                        )
                ).asUpdate
        )

        return antall
    }
}
