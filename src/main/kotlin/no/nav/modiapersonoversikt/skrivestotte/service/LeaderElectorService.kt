package no.nav.modiapersonoversikt.skrivestotte.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.utils.fromJson
import no.nav.modiapersonoversikt.utils.measureTimeMillis
import no.nav.personoversikt.common.utils.SelftestGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetAddress
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.seconds

private val client = HttpClient(Apache)

private val log: Logger = LoggerFactory.getLogger(LeaderElectorService::class.java)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LeaderElectorResponse(val name: String)

class LeaderElectorService(val configuration: Configuration) {
    private val selftest = SelftestGenerator.Reporter("LeaderElectorService", false)

    init {
        fixedRateTimer("LeaderElectorService check", daemon = true, initialDelay = 0, period = 10.seconds.inWholeMilliseconds) {
            runBlocking {
                selftest.ping {
                    getLeader()
                }
            }
        }
    }

    fun isLeader(): Boolean {
        return measureTimeMillis("isLeader") {
            if (configuration.clusterName == "local") {
                true
            } else {
                val leader = getLeader()
                val hostname = InetAddress.getLocalHost().hostName

                hostname == leader.name
            }
        }
    }

    internal fun getLeader(): LeaderElectorResponse {
        return runCatching { getLeaderOrThrow() }
            .onFailure { log.error("Could not get leader from ${configuration.electorPath}", it) }
            .getOrDefault(LeaderElectorResponse(""))
    }

    private fun getLeaderOrThrow(): LeaderElectorResponse = runBlocking {
        val response = client.get(configuration.electorPath)
        response.bodyAsText().fromJson()
    }
}
