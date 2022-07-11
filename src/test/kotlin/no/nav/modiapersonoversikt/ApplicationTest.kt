package no.nav.modiapersonoversikt

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import no.nav.modiapersonoversikt.skrivestotte.model.Tekst
import no.nav.modiapersonoversikt.skrivestotte.model.Tekster
import no.nav.modiapersonoversikt.utils.fromJson
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class ApplicationTest : WithDatabase {
    @Test
    fun `should load backup if database is empty`() {
        withTestApp(connectionUrl()) {
            val response = getTexts()
            assertEquals(response.status, 200)
            assertEquals(response.data.size, 811)
        }
    }
    
    @Test
    fun `should filter resultset based on tags`() {
        withTestApp(connectionUrl()) {
            val response = getTexts(tags = listOf("sto"))
            assertEquals(response.status, 200)
            assertEquals(response.data.size, 231)
        }
    }
}

class JsonResponse<T>(val status: Int, val data: T)

suspend fun ApplicationTestBuilder.getTexts(
    tags: List<String> = emptyList(),
    usageSort: Boolean = false
): JsonResponse<Tekster> {
    val queryParams = tags
        .map { "tags" to it }
        .plus("usageSort" to usageSort)
        .map { "${it.first}=${it.second}" }
        .joinToString("&")
        .let { if (it.isNotEmpty()) "?$it" else "" }
    
    val response = client.get("/modiapersonoversikt-skrivestotte/skrivestotte$queryParams")
    val data = response.bodyAsText().fromJson<Map<UUID, Tekst>>()
    return JsonResponse(response.status.value, data)
}
