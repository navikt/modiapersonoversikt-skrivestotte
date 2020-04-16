package no.nav.modiapersonoversikt.storage

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.modiapersonoversikt.Configuration
import no.nav.modiapersonoversikt.toEpochMillis
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import javax.sql.DataSource
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log

const val table = "statistikk"
const val rawTable = "statistikk_raw"
const val retentionDays = 60L

data class StatistikkEntry(val tidspunkt: Long, val antall: Int)
data class DetaljertStatistikk(val id: UUID, val overskrift: String, val tags: List<String>, val vekttall: Int)

fun Row.intOr(name: String, default: Int): Int = try { this.int(name) } catch (_: Throwable) { default };

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

    override fun hentOverordnetBruk(): List<StatistikkEntry> {
        return transactional(dataSource) { tx -> hentTidsgruppertData(tx, 2, PostgreSqlIntervalUnits.HOURS) }
    }

    override fun hentDetaljertBruk(from: LocalDateTime, to: LocalDateTime): List<DetaljertStatistikk> {
        return transactional(dataSource) { tx ->
            tx.run(queryOf("""
                SELECT id, overskrift, tags, antall 
                FROM tekst t
                JOIN (
                	SELECT tekstid, count(*) AS antall 
                	FROM $rawTable
                	WHERE tidspunkt >= :from AND tidspunkt <= :to
                	GROUP BY tekstid
                ) s ON (s.tekstid = t.id)
                ORDER BY antall DESC
            """.trimIndent(), mapOf("from" to from, "to" to to))
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

    fun hentTidsgruppertData(tx: Session, amount: Int, unit: PostgreSqlIntervalUnits): List<StatistikkEntry> {
        val sql = hentGrupperteDataSql(amount, unit)
        println(sql)

        val hourScale = if (unit == PostgreSqlIntervalUnits.HOURS) amount else 1
        val minuteScale = if (unit == PostgreSqlIntervalUnits.MINUTES) amount else 1
        val secondsScale = if (unit == PostgreSqlIntervalUnits.SECONDS) amount else 1

        return tx.run(queryOf(sql)
                .map { row ->
                    val year = row.int("years")
                    val month = row.int("months")
                    val day = row.int("days")
                    val hour = row.intOr("hours", 0) * hourScale
                    val minute = row.intOr("minutes", 0) * minuteScale
                    val second = row.intOr("seconds", 0) * secondsScale

                    val tidspunkt = LocalDateTime.of(year, month, day, hour, minute, second)
                            .atZone(ZoneId.systemDefault())
                            .toEpochMillis()

                    StatistikkEntry(
                            tidspunkt,
                            row.int("count")
                    )
                }.asList
        )
    }

    private fun hentGrupperteDataSql(amount: Int, unit: PostgreSqlIntervalUnits): String {
        val sqlList = mutableListOf<String>()
        sqlList.add("select")

        for (chronoUnit in PostgreSqlIntervalUnits.values()) {
            if (chronoUnit == unit) {
                break
            }
            val sqlChronoUnit = chronoUnit.name.toLowerCase()

            sqlList.add("extract($sqlChronoUnit from tidspunkt) as $sqlChronoUnit,")
        }
        val sqlUnit = unit.name.toLowerCase()
        sqlList.add("trunc(extract($sqlUnit from tidspunkt) / $amount) as $sqlUnit,")
        sqlList.add("count(*) as count")
        sqlList.add("from $rawTable")
        sqlList.add("group by ")

        for (chronoUnit in PostgreSqlIntervalUnits.values()) {
            val sqlChronoUnit = chronoUnit.name.toLowerCase()
            sqlList.add("$sqlChronoUnit, ")
            if (chronoUnit == unit) {
                break
            }
        }

        return sqlList.joinToString("\n").removeSuffix(", ")
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
    YEARS(ChronoUnit.YEARS),
    MONTHS(ChronoUnit.MONTHS),
    DAYS(ChronoUnit.DAYS),
    HOURS(ChronoUnit.HOURS),
    MINUTES(ChronoUnit.MINUTES),
    SECONDS(ChronoUnit.SECONDS)
}
