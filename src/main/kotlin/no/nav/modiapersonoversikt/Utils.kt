package no.nav.modiapersonoversikt

import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.Application")
fun <T : Any> measureTimeMillis(name: String, fn: () -> T): T {
    val start = System.currentTimeMillis()
    val response = fn()
    val end = System.currentTimeMillis()
    log.info("Timedtask: $name Duration: ${end - start}ms")
    return response
}

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun ZonedDateTime.toEpochMillis(): Long {
    return this.toEpochSecond() * 1000
}
