package no.nav.modiapersonoversikt.config

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.personoversikt.ktor.utils.OidcClient
import no.nav.personoversikt.ktor.utils.Security
import no.nav.personoversikt.ktor.utils.Security.AuthProviderConfig
import no.nav.personoversikt.utils.EnvUtils.getRequiredConfig

private val defaultValues = mapOf(
    "NAIS_CLUSTER_NAME" to "local",
    "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
    "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
    "DATABASE_JDBC_URL" to "jdbc:postgresql://localhost:5432/modiapersonoversikt-skrivestotte",
    "VAULT_MOUNTPATH" to "",
    "ELECTOR_PATH" to "",
    "USE_STATISTICS_SORT" to "false",
    "AZURE_APP_WELL_KNOWN_URL" to "http://localhost",
    "AZURE_APP_CLIENT_ID" to "",
    "AZURE_APP_CLIENT_SECRET" to "",
)

data class DatabaseConfig(
    val jdbcUrl: String = getRequiredConfig("DATABASE_JDBC_URL", defaultValues),
    val vaultMountpath: String = getRequiredConfig("VAULT_MOUNTPATH", defaultValues),
)
data class AzureAdConfig(
    val wellKnownUrl: String = getRequiredConfig("AZURE_APP_WELL_KNOWN_URL", defaultValues),
    val clientId: String = getRequiredConfig("AZURE_APP_CLIENT_ID", defaultValues),
    val clientSecret: String = getRequiredConfig("AZURE_APP_CLIENT_SECRET", defaultValues),
) {
    private val client = OidcClient(wellKnownUrl)
    val oidc: OidcClient.OidcDiscoveryConfig by lazy {
        runBlocking {
            client.fetch()
        }
    }

    val providerConfig: AuthProviderConfig by lazy {
        AuthProviderConfig(
            name = "azuread",
            jwksConfig = Security.JwksConfig.JwksUrl(oidc.jwksUrl, oidc.issuer),
            tokenLocations = listOf(
                Security.TokenLocation.Header(HttpHeaders.Authorization)
            )
        )
    }
}

class Configuration(
    val clusterName: String = getRequiredConfig("NAIS_CLUSTER_NAME", defaultValues),
    val openam: AuthProviderConfig = AuthProviderConfig(
        name = "openam",
        jwksConfig = Security.JwksConfig.JwksUrl(
            jwksUrl = getRequiredConfig("ISSO_JWKS_URL", defaultValues),
            issuer = getRequiredConfig("ISSO_ISSUER", defaultValues),
        ),
        tokenLocations = listOf(
            Security.TokenLocation.Cookie("modia_ID_token"),
            Security.TokenLocation.Cookie("ID_token"),
        ),
        overrides = {
            // Disable jwt challenge to allow oauth-flow to happen
            challenge { _, _ ->  }
        }
    ),
    val azuread: AzureAdConfig = AzureAdConfig(),
    val database: DatabaseConfig = DatabaseConfig(),
    val electorPath: String = createUrl(getRequiredConfig("ELECTOR_PATH", defaultValues)),
    val useStatisticsSort: Boolean = getRequiredConfig("USE_STATISTICS_SORT", defaultValues).toBoolean()
)

private fun createUrl(path: String): String =
    if (path.startsWith("http")) {
        path
    } else {
        "http://$path"
    }
