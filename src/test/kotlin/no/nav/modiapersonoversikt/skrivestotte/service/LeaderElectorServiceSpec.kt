package no.nav.modiapersonoversikt.skrivestotte.service

import no.nav.modiapersonoversikt.config.Configuration
import no.nav.modiapersonoversikt.utils.fromJson
import no.nav.modiapersonoversikt.configureMockserver
import no.nav.modiapersonoversikt.run
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class LeaderElectorServiceSpec {
    @Test
    fun `Running locally`() {
        configureMockserver {
            MockResponse().setBody("{\"name\":\"inethostname\"}")
        }.run {
            val service = LeaderElectorService(Configuration(electorPath = url("").toString()))
            assertTrue(service.isLeader())
        }
    }

    @Test
    fun `Running on nais`() {
        configureMockserver {
            MockResponse().setBody("{\"name\":\"inethostname\"}")
        }.run {
            val service = LeaderElectorService(Configuration(electorPath = url("").toString(), clusterName = "something else"))
            assertEquals("inethostname", service.getLeader().name)
        }
    }

    @Test
    fun `Unknown field is ignored`() {
        assertDoesNotThrow {
            "{\"name\":\"inethostname\", \"unknownfield\":\"othervalue\"}".fromJson<LeaderElectorResponse>()
        }
    }
}
