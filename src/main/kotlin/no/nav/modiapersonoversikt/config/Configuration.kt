package no.nav.modiapersonoversikt.config

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.personoversikt.ktor.utils.OidcClient
import no.nav.personoversikt.ktor.utils.Security
import no.nav.personoversikt.ktor.utils.Security.AuthProviderConfig
import no.nav.personoversikt.utils.EnvUtils.getRequiredConfig

private val defaultValues = mapOf(
    "NAIS_CLUSTER_NAME" to "local",
    "DATABASE_JDBC_URL" to "jdbc:postgresql://localhost:5432/modiapersonoversikt-skrivestotte",
    "VAULT_MOUNTPATH" to "",
    "ELECTOR_PATH" to "",
    "USE_STATISTICS_SORT" to "false",
    "AZURE_APP_WELL_KNOWN_URL" to "http://localhost",
    "AZURE_APP_CLIENT_ID" to "",
    "AZURE_APP_CLIENT_SECRET" to "",
    "DB_NAME" to "modiapersonoversikt-skrivestotte-pg15"
)

data class DatabaseConfig(
    val jdbcUrl: String = getRequiredConfig("DATABASE_JDBC_URL", defaultValues),
    val vaultMountpath: String = getRequiredConfig("VAULT_MOUNTPATH", defaultValues),
    val databaseName: String = getRequiredConfig("DB_NAME", defaultValues)
)
data class AzureAdConfig(
    val wellKnownUrl: String = getRequiredConfig("AZURE_APP_WELL_KNOWN_URL", defaultValues),
    val clientId: String = getRequiredConfig("AZURE_APP_CLIENT_ID", defaultValues),
    val clientSecret: String = getRequiredConfig("AZURE_APP_CLIENT_SECRET", defaultValues)
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
            ),
            overrides = {
                challenge { _, _ -> }
            }
        )
    }
}

class Configuration(
    val clusterName: String = getRequiredConfig("NAIS_CLUSTER_NAME", defaultValues),
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
