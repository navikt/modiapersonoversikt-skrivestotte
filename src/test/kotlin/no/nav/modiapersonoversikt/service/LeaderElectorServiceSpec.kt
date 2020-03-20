package no.nav.modiapersonoversikt.service

import no.nav.modiapersonoversikt.Configuration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.*


object LeaderElectorServiceSpec : Spek({
    val mockserver = MockWebServer()
    mockserver.enqueue(MockResponse().setBody("{\"name\":\"inethostname\"}"))
    mockserver.start()

    val electorPath = mockserver.url("").toString()

    given("Running locally") {
        val service = LeaderElectorService(Configuration(electorPath = electorPath))
        on("requesting leader") {
            it("should return true") {
                val isLeader = service.isLeader()
                assertTrue(isLeader)
            }
        }
    }

    given("Running on nais") {
        val service = LeaderElectorService(Configuration(electorPath = electorPath, clusterName = "something else"))
        on("requesting leader") {
            it("should be able to parse response") {
                val leader = service.getLeader()
                assertEquals("inethostname", leader.name)
            }
        }
    }
})
