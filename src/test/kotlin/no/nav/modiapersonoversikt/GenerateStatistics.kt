package no.nav.modiapersonoversikt

import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import no.nav.modiapersonoversikt.storage.JdbcStorageProvider
import no.nav.modiapersonoversikt.storage.rawTable
import no.nav.modiapersonoversikt.storage.transactional
import java.io.File
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

fun List<Int>.normalize(): List<Double> {
    val sum = this.sum().toDouble()
    return this.map { it / sum }
}

fun Int.between(min: Int, max: Int): Int {
    return max(min(this, max - 1), min)
}

@ExperimentalStdlibApi
fun List<Double>.cumulativeSum(): List<Double> {
    return this.scanReduce { acc, element -> acc + element }
}

@ExperimentalStdlibApi
fun generateStatistics(dataSource: DataSource, storage: JdbcStorageProvider, frequencies: List<Int>) = runBlocking {
    val alleTekster = storage.hentTekster(null, false)
    val keys = alleTekster.keys.toList().shuffled()
    transactional(dataSource) { tx ->
        repeat(10) { dayOffset ->
            val startTime = LocalDateTime.now()
                .minusDays(dayOffset + 1L)
                .withMinute(0)
                .withSecond(0)

            val sum = frequencies.sum()
            val normalizedFrequencies = frequencies
                .normalize()
                .cumulativeSum()

            println(sum)

            val rndGenerator = java.util.Random()
            println(normalizedFrequencies)
            repeat(sum) {
                val rnd = rndGenerator.nextDouble()
                val index = normalizedFrequencies.indexOfFirst { frequency -> rnd < frequency }
                val hour = (rndGenerator.nextGaussian() * 3 + 12).toInt().between(0, 24)
                val date = startTime
                    .withMinute(Random.nextInt(60))
                    .withSecond(Random.nextInt(60))
                    .withHour(hour)
                val key = keys[index]

                tx.run(
                    queryOf(
                        "INSERT INTO $rawTable (tidspunkt, tekstid) VALUES(:tidspunkt, :id)",
                        mapOf(
                            "tidspunkt" to date,
                            "id" to key.toString()
                        )
                    ).asUpdate
                )

                if (it % 100 == 0) {
                    println(it)
                }
            }
        }

        val result = tx.run(
            queryOf("SELECT tidspunkt, tekstid FROM $rawTable")
                .map { row ->
                    Pair(
                        Timestamp.valueOf(row.string("tidspunkt")).toLocalDateTime(),
                        row.string("tekstid")
                    )
                }.asList
        )
            .groupBy {
                it.first.hour
            }
            .mapValues { entry -> entry.value.size }
            .entries
            .sortedBy { it.key }

        println(result)
    }
}

class App

@ExperimentalStdlibApi
fun main() {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)
    DataSourceConfiguration.migrateDb(dbConfig.userDataSource())
    val storage = JdbcStorageProvider(dbConfig.userDataSource(), configuration)
    val frequencies = File("src/test/kotlin/no/nav/modiapersonoversikt/frequencies.txt")
        .readLines()
        .map { it.toInt() }

    generateStatistics(dbConfig.userDataSource(), storage, frequencies)
}
