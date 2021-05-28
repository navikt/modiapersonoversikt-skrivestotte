package no.nav.modiapersonoversikt

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.Application")
suspend fun <T : Any> measureTimeMillisSuspended(name: String, fn: suspend () -> T): T {
    val start = System.currentTimeMillis()
    val response = fn()
    val end = System.currentTimeMillis()
    log.info("Timedtask: $name Duration: ${end - start}ms")
    return response
}
fun <T : Any> measureTimeMillis(name: String, fn: suspend () -> T): T {
    return runBlocking {
        measureTimeMillisSuspended(name) {
            fn()
        }
    }
}

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun ZonedDateTime.toEpochMillis(): Long {
    return this.toEpochSecond() * 1000
}
