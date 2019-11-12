package no.nav.modiapersonoversikt

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.*

private const val notUsedLocally = ""
private val defaultProperties = ConfigurationMap(
        mapOf(
                "NAIS_CLUSTER_NAME" to notUsedLocally,
                "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
                "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
                "DATABASE_HOST" to "",
                "DATABASE_PORT" to "",
                "DATABASE_NAME" to "modiapersonoversikt-skrivestotte"
        )
)

data class Configuration(
        val clusterName: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
        val jwksUrl: JwkProvider = JwtUtil.makeJwkProvider(config()[Key("ISSO_JWKS_URL", stringType)]),
        val jwtIssuer: String = config()[Key("ISSO_ISSUER", stringType)]
)

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties