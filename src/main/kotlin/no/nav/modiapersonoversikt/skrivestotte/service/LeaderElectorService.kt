package no.nav.modiapersonoversikt.skrivestotte.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.utils.fromJson
import no.nav.modiapersonoversikt.utils.measureTimeMillis
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.InetAddress

private val client = HttpClient(Apache)

private val log: Logger = LoggerFactory.getLogger(LeaderElectorService::class.java)
data class LeaderElectorResponse(val name: String)

class LeaderElectorService(val configuration: Configuration) {
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
        return try {
            runBlocking {
                val response = client.get(configuration.electorPath)
                response.bodyAsText().fromJson()
            }
        } catch (e: Exception) {
            log.error("Could not get leader from ${configuration.electorPath}", e)
            LeaderElectorResponse("")
        }
    }
}
