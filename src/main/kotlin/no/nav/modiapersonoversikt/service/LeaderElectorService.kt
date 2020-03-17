package no.nav.modiapersonoversikt.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import no.nav.modiapersonoversikt.Configuration
import no.nav.modiapersonoversikt.measureTimeMillis
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.InetAddress

private val clientHolder = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
}

private val log: Logger = LoggerFactory.getLogger(LeaderElectorService::class.java)
data class LeaderElectorResponse(val name: String)

class LeaderElectorService(val configuration: Configuration) {
    fun isLeader(): Boolean {
        return measureTimeMillis("isLeader") {
            if (configuration.clusterName == "local") {
                true
            } else {
                val leader = getLeader()
                val hostname = InetAddress.getLocalHost().hostAddress

                hostname == leader.name
            }

        }
    }

    private fun getLeader(): LeaderElectorResponse {
        return try {
            runBlocking {
                clientHolder.use{ client -> client.get<LeaderElectorResponse>(configuration.electorPath) }
            }
        } catch (e: Exception) {
            log.error("Could not get leader from ${configuration.electorPath}", e)
            LeaderElectorResponse("")
        }
    }
}
