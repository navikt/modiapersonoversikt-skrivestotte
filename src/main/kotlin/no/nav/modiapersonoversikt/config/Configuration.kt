package no.nav.modiapersonoversikt.config

import no.nav.modiapersonoversikt.AzureAd
import no.nav.modiapersonoversikt.OpenAM
import no.nav.personoversikt.ktor.utils.Security.AuthCookie
import no.nav.personoversikt.ktor.utils.Security.AuthProviderConfig
import no.nav.personoversikt.utils.ConditionalUtils.ifNotNull
import no.nav.personoversikt.utils.EnvUtils.getConfig
import no.nav.personoversikt.utils.EnvUtils.getRequiredConfig

private val defaultValues = mapOf(
    "NAIS_CLUSTER_NAME" to "local",
    "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
    "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
    "DATABASE_JDBC_URL" to "jdbc:postgresql://localhost:5432/modiapersonoversikt-skrivestotte",
    "VAULT_MOUNTPATH" to "",
    "ELECTOR_PATH" to "",
    "USE_STATISTICS_SORT" to "false"
)

data class DatabaseConfig(
    val jdbcUrl: String = getRequiredConfig("DATABASE_JDBC_URL", defaultValues),
    val vaultMountpath: String = getRequiredConfig("VAULT_MOUNTPATH", defaultValues),
)

class Configuration(
    val clusterName: String = getRequiredConfig("NAIS_CLUSTER_NAME", defaultValues),
    val openam: AuthProviderConfig = AuthProviderConfig(
        name = OpenAM,
        jwksUrl = getRequiredConfig("ISSO_JWKS_URL", defaultValues),
        cookies = listOf(
            AuthCookie("modia_ID_token"),
            AuthCookie("ID_token")
        )
    ),
    val azuread: AuthProviderConfig? = ifNotNull(
        getConfig("AZURE_OPENID_CONFIG_JWKS_URI", defaultValues),
        getConfig("SECRET", defaultValues)
    ) { jwksurl, secret ->
        AuthProviderConfig(
            name = AzureAd,
            jwksUrl = jwksurl,
            cookies = listOf(
                AuthCookie(
                    name = "modiapersonoversikt_tokens",
                    encryptionKey = secret
                )
            )
        )
    },
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
