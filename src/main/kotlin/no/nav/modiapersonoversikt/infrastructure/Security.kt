package no.nav.modiapersonoversikt.infrastructure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import no.nav.modiapersonoversikt.config.AuthCookie
import no.nav.modiapersonoversikt.config.AuthProviderConfig
import no.nav.modiapersonoversikt.log
import java.net.URL
import java.util.concurrent.TimeUnit

class SubjectPrincipal(val subject: String) : Principal
class Security(private val providers: List<AuthProviderConfig>) {
    companion object {
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

    private val cryptermap = providers
        .flatMap { it.cookies }
        .mapNotNull { it.encryptedWithSecret }
        .associateWith { Crypter(it) }

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
                if (provider.cookies.isNotEmpty()) {
                    authHeader {
                        parseAuthorizationHeader(getToken(it, provider.cookies) ?: "")
                    }
                }
                verifier(makeJwkProvider(provider.jwksUrl))
                validate { validateJWT(it) }
            }
        }
    }

    fun getSubject(call: ApplicationCall): List<String> {
        return providers.map { getSubject(call, it.cookies) }
    }

    private fun getSubject(call: ApplicationCall, cookies: List<AuthCookie>): String {
        return try {
            getToken(call, cookies)
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

    private fun getToken(call: ApplicationCall, cookies: List<AuthCookie>): String? {
        return call.request.header(HttpHeaders.Authorization) ?: getFromCookies(call, cookies)
    }

    private fun getFromCookies(call: ApplicationCall, cookies: List<AuthCookie>): String? {
        return cookies
            .findFirstMatching(call)
            ?.getValue(call)
            ?.let {
                if (it.startsWith("bearer", ignoreCase = true)) {
                    it
                } else {
                    "Bearer $it"
                }
            }
    }

    private fun List<AuthCookie>.findFirstMatching(call: ApplicationCall): AuthCookie? {
        return this.find { call.request.cookies[it.name].isNullOrEmpty().not() }
    }

    private fun AuthCookie.getValue(call: ApplicationCall): String? {
        val value = call.request.cookies[this.name] ?: return null
        val crypter = cryptermap[this.encryptedWithSecret] ?: return value

        return crypter.decryptSafe(value).getOrNull()
    }
}
