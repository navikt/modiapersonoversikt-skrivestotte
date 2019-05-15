package no.nav.modiapersonoversikt

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.JwtConfig")

fun makeJwkProvider(jwksUrl: String): JwkProvider =
        JwkProviderBuilder(URL(jwksUrl))
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

fun validateJWT(credentials: JWTCredential): Principal? {
    return try {
        requireNotNull(credentials.payload.audience) {"Audience not present"}
        JWTPrincipal(credentials.payload)
    } catch (e: Exception) {
        log.error("Failed to validateJWT token", e)
        null
    }
}