package no.nav.modiapersonoversikt.skrivestotte.storage

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.utils.toEpochMillis
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import javax.sql.DataSource

const val table = "statistikk"
const val rawTable = "statistikk_raw"
const val retentionDays = 60L

data class StatistikkEntry(val tidspunkt: Long, val antall: Int)
data class DetaljertStatistikk(val id: UUID, val overskrift: String, val tags: List<String>, val vekttall: Int)

fun Row.intOr(name: String, default: Int): Int = try { this.int(name) } catch (_: Throwable) { default }

class JdbcStatisticsProvider(private val dataSource: DataSource, private val configuration: Configuration) : StatisticsProvider {
    override suspend fun hentStatistikk(): Map<UUID, Int> {
        return transactional(dataSource) { tx ->
            val data = tx.run(
                queryOf("SELECT * FROM $table")
                    .map { row ->
                        UUID.fromString(row.string("id")) to row.int("brukt")
                    }
                    .asList
            )
            mapOf(*(data.toTypedArray()))
        }
    }

    override suspend fun rapporterBruk(id: UUID): Int {
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

    override suspend fun refreshStatistikk() {
        transactional(dataSource) { tx -> refreshStatistikk(tx) }
    }

    override suspend fun hentOverordnetBruk(): List<StatistikkEntry> {
        return transactional(dataSource) { tx ->
            val sql = """
            SELECT
                EXTRACT(YEARS from tidspunkt) as years,
                EXTRACT(MONTHS from tidspunkt) as months,
                EXTRACT(DAYS from tidspunkt) as days,
                TRUNC(EXTRACT(HOURS from tidspunkt) / 2) as hours,
                COUNT(*) as count
            FROM $rawTable
            GROUP BY years, months, days, hours
            """.trimIndent()

            tx.run(
                queryOf(sql)
                    .map { row ->
                        val year = row.int("years")
                        val month = row.int("months")
                        val day = row.int("days")
                        val hour = row.intOr("hours", 0) * 2
                        val minute = row.intOr("minutes", 0)
                        val second = row.intOr("seconds", 0)

                        val tidspunkt = LocalDateTime.of(year, month, day, hour, minute, second)
                            .atZone(ZoneId.systemDefault())
                            .toEpochMillis()

                        StatistikkEntry(
                            tidspunkt,
                            row.int("count")
                        )
                    }.asList
            )
                .sortedBy { it.tidspunkt }
        }
    }

    override suspend fun hentDetaljertBruk(from: LocalDateTime, to: LocalDateTime): List<DetaljertStatistikk> {
        return transactional(dataSource) { tx ->
            tx.run(
                queryOf(
                    """
                SELECT id, overskrift, tags, antall 
                FROM tekst t
                JOIN (
                	SELECT tekstid, count(*) AS antall 
                	FROM $rawTable
                	WHERE tidspunkt >= :from AND tidspunkt <= :to
                	GROUP BY tekstid
                ) s ON (s.tekstid = t.id)
                ORDER BY antall DESC
            """.trimIndent(),
                    mapOf("from" to from, "to" to to)
                )
                    .map { row ->
                        DetaljertStatistikk(
                            UUID.fromString(row.string("id")),
                            row.string("overskrift"),
                            row.string("tags").split("|"),
                            row.int("antall")
                        )
                    }
                    .asList
            )
        }
    }

    override suspend fun slettAllStatistikk() {
        transactional(dataSource) { tx ->
            tx.run(queryOf("DELETE FROM $rawTable").asUpdate)
            tx.run(queryOf("DELETE FROM $table").asUpdate)
        }
    }

    private fun slettGamleRawInnslag(tx: Session) {
        tx.run(
            queryOf("DELETE FROM $rawTable WHERE tidspunkt < now() - ${createSqlInterval(retentionDays, PostgreSqlIntervalUnits.DAYS)}")
                .asUpdate
        )
    }

    private fun hentAlleRawInnslag(tx: Session): List<Pair<String, Int>> {
        return tx.run(
            queryOf("SELECT tekstid, count(*) as antall FROM $rawTable WHERE tidspunkt > now() - ${createSqlInterval(7, PostgreSqlIntervalUnits.DAYS)} GROUP BY tekstid")
                .map { row -> Pair(row.string("tekstid"), row.int("antall")) }
                .asList
        )
    }

    private fun slettStatistikk(tx: Session) {
        tx.run(queryOf("DELETE FROM $table").asUpdate)
    }

    private fun refreshStatistikk(tx: Session) {
        slettGamleRawInnslag(tx)
        val data: List<Pair<String, Int>> = hentAlleRawInnslag(tx)
        slettStatistikk(tx)
        oppdatererStatistikk(tx, data)
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
        if (configuration.database.jdbcUrl.contains(":h2:")) {
            val fraction = Duration.of(amount, unit.chronoUnit).seconds.toDouble() / secondsInADay
            return fraction.toString()
        }
        return "INTERVAL '$amount ${unit.name}'"
    }
}

val secondsInADay: Long = Duration.ofDays(1).seconds
enum class PostgreSqlIntervalUnits(val chronoUnit: ChronoUnit) {
    YEARS(ChronoUnit.YEARS),
    MONTHS(ChronoUnit.MONTHS),
    DAYS(ChronoUnit.DAYS),
    HOURS(ChronoUnit.HOURS),
    MINUTES(ChronoUnit.MINUTES),
    SECONDS(ChronoUnit.SECONDS)
}
