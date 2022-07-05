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

fun getConfig(name: String, defaultValues: Map<String, String?> = emptyMap()): String? {
    return System.getProperty(name) ?: System.getenv(name) ?: defaultValues[name]
}

fun getRequiredConfig(name: String, defaultValues: Map<String, String?> = emptyMap()): String =
    requireNotNull(getConfig(name, defaultValues)) {
        "Could not find property/env for '$name'"
    }

fun allNotNull(first: String?, second: String?): Pair<String, String>? {
    return first?.let { a -> second?.let { b -> a to b } }
}