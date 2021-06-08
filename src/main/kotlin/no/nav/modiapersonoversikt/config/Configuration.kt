package no.nav.modiapersonoversikt.config

import com.natpryce.konfig.*

private val defaultProperties = ConfigurationMap(
    mapOf(
        "NAIS_CLUSTER_NAME" to "local",
        "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
        "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
//                "DATABASE_JDBC_URL" to "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
//                "DATABASE_JDBC_URL" to "jdbc:h2:tcp://localhost:8090/./testdb",
        "DATABASE_JDBC_URL" to "jdbc:postgresql://localhost:5432/modiapersonoversikt-skrivestotte",
        "VAULT_MOUNTPATH" to "",
        "ELECTOR_PATH" to "",
        "USE_STATISTICS_SORT" to "false"
    )
)

data class Configuration(
    val clusterName: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
    val jwksUrl: String = config()[Key("ISSO_JWKS_URL", stringType)],
    val jwtIssuer: String = config()[Key("ISSO_ISSUER", stringType)],
    val jdbcUrl: String = config()[Key("DATABASE_JDBC_URL", stringType)],
    val vaultMountpath: String = config()[Key("VAULT_MOUNTPATH", stringType)],
    val electorPath: String = createUrl(config()[Key("ELECTOR_PATH", stringType)]),
    val useStatisticsSort: Boolean = config()[Key("USE_STATISTICS_SORT", booleanType)]
)

private fun createUrl(path: String): String =
    if (path.startsWith("http")) {
        path
    } else {
        "http://$path"
    }

private fun config() = ConfigurationProperties.systemProperties() overriding
    EnvironmentVariables overriding
    defaultProperties
