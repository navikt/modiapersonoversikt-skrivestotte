package no.nav.modiapersonoversikt.skrivestotte.service

import no.nav.modiapersonoversikt.config.Configuration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class LeaderElectorServiceSpec {
    val mockserver = MockWebServer()
    val electorPath = mockserver.url("").toString()
    init {
        mockserver.enqueue(MockResponse().setBody("{\"name\":\"inethostname\", \"last_update\":\"2022-07-12T09:59:26Z\"}"))
    }

    @Test
    fun `Running locally`() {
        val service = LeaderElectorService(Configuration(electorPath = electorPath))
        assertTrue(service.isLeader())
    }

    @Test
    fun `Running on nais`() {
        val service = LeaderElectorService(Configuration(electorPath = electorPath, clusterName = "something else"))
        assertEquals("inethostname", service.getLeader().name)
    }

    @Test
    fun `Sjekke om ignorerer ukjent variabel`() {
        val service = LeaderElectorService(Configuration(electorPath = electorPath))
        assertEquals(LeaderElectorResponse(name="inethostname"), service.getLeader())
    }
}
