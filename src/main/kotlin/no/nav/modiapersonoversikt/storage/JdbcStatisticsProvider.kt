package no.nav.modiapersonoversikt.storage

import kotliquery.Session
import kotliquery.queryOf
import no.nav.modiapersonoversikt.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import javax.sql.DataSource

const val table = "statistikk"
const val rawTable = "statistikk_raw"

class JdbcStatisticsProvider(private val dataSource: DataSource, private val configuration: Configuration) : StatisticsProvider {
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
            tx.run(
                    queryOf(
                            "INSERT INTO $rawTable (tekstid) VALUES(:id)",
                            mapOf("id" to id.toString())
                    )
                            .asUpdate
            )
        }
    }

    override fun refreshStatistikk() {
        transactional(dataSource) { tx ->
            slettGamleRawInnslag(tx)
            val data : List<Pair<String, Int>> = hentAlleRawInnslag(tx)
            slettStatistikk(tx)
            oppdatererStatistikk(tx, data)
        }
    }

    private fun slettGamleRawInnslag(tx: Session) {
        tx.run(
                queryOf("DELETE FROM $rawTable WHERE tidspunkt < now() - ${createSqlInterval(7, PostgreSqlIntervalUnits.DAYS)}")
                        .asUpdate
        )
    }

    private fun hentAlleRawInnslag(tx: Session): List<Pair<String, Int>> {
        return tx.run(
                queryOf("SELECT tekstid, count(*) as antall FROM $rawTable GROUP BY tekstid")
                        .map { row -> Pair(row.string("tekstid"), row.int("antall")) }
                        .asList
        )
    }

    private fun slettStatistikk(tx: Session) {
        tx.run(queryOf("DELETE FROM $table").asUpdate)
    }

    private fun oppdatererStatistikk(tx: Session, data: List<Pair<String, Int>>) {
        data
                .forEach { (tekstId, antall) ->
                    tx.run(
                            queryOf(
                                    "INSERT INTO $table (id, brukt) VALUES (:id, :antall)",
                                    mapOf(
                                            "id" to tekstId,
                                            "antall" to antall
                                    )
                            )
                                    .asUpdate
                    )
                }

    }

    private fun createSqlInterval(amount: Long, unit: PostgreSqlIntervalUnits): String {
        if (configuration.jdbcUrl.contains(":h2:")) {
            val fraction = Duration.of(amount, unit.chronoUnit).seconds.toDouble() / secondsInADay;
            return fraction.toString()
        }
        return "INTERVAL '$amount ${unit.name}'"
    }
}

val secondsInADay : Long = Duration.ofDays(1).seconds
enum class PostgreSqlIntervalUnits(val chronoUnit: ChronoUnit) {
    SECONDS(ChronoUnit.SECONDS),
    MINUTES(ChronoUnit.MINUTES),
    HOURS(ChronoUnit.HOURS),
    DAYS(ChronoUnit.DAYS),
    MONTHS(ChronoUnit.MONTHS),
    YEARS(ChronoUnit.YEARS)
}
