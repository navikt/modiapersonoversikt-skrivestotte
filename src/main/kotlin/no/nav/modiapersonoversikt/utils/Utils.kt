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

inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> ifNotNull(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}