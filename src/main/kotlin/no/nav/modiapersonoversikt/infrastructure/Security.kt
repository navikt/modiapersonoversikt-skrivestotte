package no.nav.modiapersonoversikt.infrastructure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import no.nav.modiapersonoversikt.config.AuthProviderConfig
import no.nav.modiapersonoversikt.log
import java.net.URL
import java.util.concurrent.TimeUnit

class SubjectPrincipal(val subject: String) : Principal
class Security(private val providers: List<AuthProviderConfig>) {
    companion object {
        private val cookieNames = listOf("modia_ID_token", "ID_token")
        const val OpenAM = "openam"
        const val AzureAD = "azuread"

        private fun Payload.getIdent(): String? {
            return getClaim("NAVident")?.asString() ?: subject
        }

        private const val authscheme = "Bearer "
        private fun removeAuthScheme(token: String): String {
            if (token.startsWith(authscheme, ignoreCase = true)) {
                return token.substring(authscheme.length)
            }
            return token
        }
    }

    context(AuthenticationConfig)
    fun setupMock(principal: SubjectPrincipal) {
        for (provider in providers) {
            val config = object : AuthenticationProvider.Config(provider.name) {}
            register(
                object : AuthenticationProvider(config) {
                    override suspend fun onAuthenticate(context: AuthenticationContext) {
                        context.principal = principal
                    }
                }
            )
        }
    }

    context(AuthenticationConfig)
    fun setupJWT() {
        for (provider in providers) {
            jwt(provider.name) {
                if (provider.usesCookies) {
                    authHeader {
                        parseAuthorizationHeader(getToken(it) ?: "")
                    }
                }
                verifier(makeJwkProvider(provider.jwksUrl))
                validate { validateJWT(it) }
            }
        }
    }
    fun getSubject(call: ApplicationCall): String {
        return try {
            getToken(call)
                ?.let(Security::removeAuthScheme)
                ?.let(JWT::decode)
                ?.getIdent()
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

    private fun validateJWT(credentials: JWTCredential): Principal? {
        return try {
            requireNotNull(credentials.payload.audience) { "Audience not present" }
            SubjectPrincipal(requireNotNull(credentials.payload.getIdent()))
        } catch (e: Exception) {
            log.error("Failed to validateJWT token", e)
            null
        }
    }

    private fun getToken(call: ApplicationCall): String? {
        return call.request.header(HttpHeaders.Authorization)
            ?: cookieNames
                .find { !call.request.cookies[it].isNullOrEmpty() }
                ?.let { call.request.cookies[it] }
                ?.let {
                    if (it.startsWith("bearer", ignoreCase = true)) {
                        it
                    } else {
                        "Bearer $it"
                    }
                }
    }
}
