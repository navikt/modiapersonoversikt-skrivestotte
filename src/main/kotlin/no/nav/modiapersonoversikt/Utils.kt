package no.nav.modiapersonoversikt

import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.Application")
fun <T : Any> measureTimeMillis(name: String, fn: () -> T): T {
    val start = System.currentTimeMillis()
    val response = fn()
    val end = System.currentTimeMillis()
    log.info("Timedtask: $name Duration: ${end - start}ms")
    return response
}
