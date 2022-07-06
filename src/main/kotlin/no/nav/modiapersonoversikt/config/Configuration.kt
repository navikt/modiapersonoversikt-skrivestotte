package no.nav.modiapersonoversikt.config

import no.nav.modiapersonoversikt.infrastructure.Security
import no.nav.modiapersonoversikt.utils.getConfig
import no.nav.modiapersonoversikt.utils.getRequiredConfig
import no.nav.modiapersonoversikt.utils.ifNotNull

private val defaultValues = mapOf(
    "NAIS_CLUSTER_NAME" to "local",
    "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
    "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
    "DATABASE_JDBC_URL" to "jdbc:postgresql://localhost:5432/modiapersonoversikt-skrivestotte",
    "VAULT_MOUNTPATH" to "",
    "ELECTOR_PATH" to "",
    "USE_STATISTICS_SORT" to "false"
)

data class AuthProviderConfig(
    val name: String,
    val jwksUrl: String,
    val issuer: String,
    val cookies: List<AuthCookie> = emptyList(),
)

class AuthCookie(
    val name: String,
    val encryptedWithSecret: String? = null
)

data class DatabaseConfig(
    val jdbcUrl: String = getRequiredConfig("DATABASE_JDBC_URL", defaultValues),
    val vaultMountpath: String = getRequiredConfig("VAULT_MOUNTPATH", defaultValues),
)

class Configuration(
    val clusterName: String = getRequiredConfig("NAIS_CLUSTER_NAME", defaultValues),
    val openam: AuthProviderConfig = AuthProviderConfig(
        name = Security.OpenAM,
        jwksUrl = getRequiredConfig("ISSO_JWKS_URL", defaultValues),
        issuer = getRequiredConfig("ISSO_ISSUER", defaultValues),
        cookies = listOf(
            AuthCookie("modia_ID_token", encryptedWithSecret = "my secret"),
            AuthCookie("ID_token")
        )
    ),
    val azuread: AuthProviderConfig? = ifNotNull(
        getConfig("AZURE_OPENID_CONFIG_JWKS_URI", defaultValues),
        getConfig("AZURE_OPENID_CONFIG_ISSUER", defaultValues),
        getConfig("SECRET", defaultValues)
    ) { jwksurl, issuer, secret ->
        AuthProviderConfig(
            name = Security.AzureAD,
            jwksUrl = jwksurl,
            issuer = issuer,
            cookies = listOf(
                AuthCookie(
                    name = "modiapersonoversikt_tokens",
                    encryptedWithSecret = secret
                )
            )
        )
    },
    val authproviders: Array<String> = listOfNotNull(openam.name, azuread?.name).toTypedArray(),
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
