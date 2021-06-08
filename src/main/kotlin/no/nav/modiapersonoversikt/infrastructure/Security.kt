package no.nav.modiapersonoversikt.infrastructure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.ApplicationCall
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.auth.HttpAuthHeader
import no.nav.modiapersonoversikt.log
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class SubjectPrincipal(val subject: String) : Principal
class Security {
    companion object {
        private val cookieNames = listOf("modia_ID_token", "ID_token")

        fun Authentication.Configuration.setupMock(mockPrincipal: SubjectPrincipal) {
            mock {
                principal = mockPrincipal
            }
        }

        fun Authentication.Configuration.setupJWT(jwksUrl: String, issuer: String) {
            jwt {
                authHeader(::useJwtFromCookie)
                verifier(
                    jwkProvider = makeJwkProvider(jwksUrl),
                    issuer = issuer
                )
                realm = "modiapersonoversikt-skrivestotte"
                validate { validateJWT(it) }
            }
        }

        fun getSubject(call: ApplicationCall): String {
            return try {
                useJwtFromCookie(call)
                    ?.getBlob()
                    ?.let { blob -> JWT.decode(blob).parsePayload().subject }
                    ?: "Unauthenticated"
            } catch (e: Throwable) {
                "JWT not found"
            }
        }

        private fun makeJwkProvider(jwksUrl: String): JwkProvider =
            JwkProviderBuilder(URL(jwksUrl))
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        private fun useJwtFromCookie(call: ApplicationCall): HttpAuthHeader? {
            return try {
                val token = cookieNames
                    .find { !call.request.cookies[it].isNullOrEmpty() }
                    ?.let { cookieName ->  call.request.cookies[cookieName] }
                io.ktor.http.auth.parseAuthorizationHeader("Bearer $token")
            } catch (ex: Throwable) {
                log.error("Could not get JWT from cookie $cookieNames", ex)
                null
            }
        }

        private fun validateJWT(credentials: JWTCredential): Principal? {
            return try {
                requireNotNull(credentials.payload.audience) { "Audience not present" }
                JWTPrincipal(credentials.payload)
            } catch (e: Exception) {
                log.error("Failed to validateJWT token", e)
                null
            }
        }

        private fun HttpAuthHeader.getBlob() = when {
            this is HttpAuthHeader.Single -> blob
            else -> null
        }

        private fun DecodedJWT.parsePayload(): Payload {
            val payloadString = String(Base64.getUrlDecoder().decode(payload))
            return JWTParser().parsePayload(payloadString)
        }
    }
}
