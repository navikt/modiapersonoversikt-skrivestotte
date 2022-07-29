package no.nav.modiapersonoversikt.infrastructure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import no.nav.modiapersonoversikt.appContextpath
import no.nav.modiapersonoversikt.config.Configuration
import no.nav.personoversikt.ktor.utils.OidcClient
import no.nav.personoversikt.ktor.utils.Security
import java.net.URL
import java.security.interfaces.RSAPublicKey

class OidcJwk(oidc: OidcClient.OidcDiscoveryConfig) {
    val issuer: String = oidc.issuer
    val jwkProvider: JwkProvider = JwkProviderBuilder(URL(oidc.jwksUrl))
        .cached(true)
        .rateLimited(true)
        .build()
}

private const val cookieName = "skrivestotte_ID_token"
private const val sessionAuthProvider = "session"
private const val oauthAuthProvider = "oauth"
private const val sessionname = "authsession"

//data class UserSession(val userId: String, val idToken: String) : Principal
typealias UserSession = String

fun Application.setupSecurity(configuration: Configuration, useMock: Boolean, runLocally: Boolean): Array<out String?> {
    val security = Security(configuration.openam)
    install(Sessions) {
        cookie<UserSession>(cookieName) {
            cookie.encoding = CookieEncoding.RAW
            cookie.path = "/$appContextpath"
            cookie.httpOnly = true
        }
    }
    install(Authentication) {
        if (useMock) {
            provider(sessionAuthProvider) {
                authenticate {
                    val token = JWT.create().withSubject("Z999999").sign(Algorithm.none())
                    it.principal(Security.SubjectPrincipal(token))
                }
            }
        } else {
            val oidc = OidcJwk(configuration.azuread.oidc)

            security.setupJWT(this)

            oauth(oauthAuthProvider) {
                urlProvider = { redirectUrl("/$appContextpath/oauth2/callback", runLocally) }
                skipWhen { call ->
                    call.sessions.get(sessionname) != null ||
                            call.request.path().endsWith("/manifest.json")
                }

                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "AAD",
                        authorizeUrl = configuration.azuread.oidc.authorizationEndpoint,
                        accessTokenUrl = configuration.azuread.oidc.tokenEndpoint,
                        requestMethod = HttpMethod.Post,
                        clientId = configuration.azuread.clientId,
                        clientSecret = configuration.azuread.clientSecret,
                        defaultScopes = listOf("openid", "offline_access", "${configuration.azuread.clientId}/.default")
                    )
                }
                client = HttpClient(CIO) {
                    this.install(ContentNegotiation) {
                        jackson()
                    }
                }
            }
            session(sessionAuthProvider) {
                skipWhen { it.request.path().endsWith("/manifest.json") }
                validate {token: String ->
                    try {
                        val idToken = JWT.decode(token)
                        checkNotNull(idToken.audience[0])
                        val jwk = oidc.jwkProvider.get(idToken.keyId)
                        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
                        val principal = Security.SubjectPrincipal(token, idToken)
                        val verifier = JWT.require(algorithm).withIssuer(oidc.issuer).build()
                        verifier.verify(idToken)
                        checkNotNull(principal.subject)
                        principal
                    } catch (_: Throwable) {
                        null
                    }
                }
                challenge {
                    call.sessions.clear(sessionname)
                    call.respondRedirect("/$appContextpath")
                }
            }
        }
    }

    val authproviders = if (useMock) arrayOf("session") else arrayOf(*security.authproviders, "oauth", "session")
    routing {
        route(appContextpath) {
            authenticate(*authproviders) {
                get("/login") {} // Needed, but is automatically redirected to oauth authorization-page

                get("/oauth2/callback") {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    val idToken = principal?.extraParameters?.get("id_token")
                    if (idToken != null) {
                        call.sessions.set(sessionname, idToken)
                        call.respondRedirect(url = "/$appContextpath", permanent = false)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Could not get accesstoken/idtoken from AAD")
                    }
                }
            }
        }
    }

    return authproviders
}

private fun ApplicationCall.redirectUrl(path: String, development: Boolean): String {
    val protocol = if (!development) "https" else request.origin.scheme
    val defaultPort = if (protocol == "http") 80 else 443
    val host = if(!development) request.host() else "localhost"

    val hostPort = host + request.port().let { port ->
        if (port == defaultPort || !development) "" else ":$port"
    }
    return "$protocol://$hostPort$path"
}