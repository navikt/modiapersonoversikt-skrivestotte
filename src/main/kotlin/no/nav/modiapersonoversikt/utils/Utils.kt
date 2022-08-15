package no.nav.modiapersonoversikt.utils

import kotlinx.coroutines.runBlocking
import no.nav.modiapersonoversikt.log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

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
