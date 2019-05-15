package no.nav.modiapersonoversikt

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.*

private val defaultProperties = ConfigurationMap(
        mapOf(
                "NAIS_CLUSTER_NAME" to "",
                "S3_ACCESS_KEY" to "",
                "S3_SECRET_KEY" to "",
                "S3_URL" to "s3.nais.preprod.local",
                "S3_REGION" to "us-east-1",
//                "SECURITY_TOKEN_SERVICE_JWKS_URL" to "https://security-token-service.nais.preprod.local/rest/v1/sts/jwks",
//                "SECURITY_TOKEN_SERVICE_ISSUER" to "https://security-token-service.nais.preprod.local"
                "SECURITY_TOKEN_SERVICE_JWKS_URL" to "https://isso-q.adeo.no:443/isso/oauth2/connect/jwk_uri",
                "SECURITY_TOKEN_SERVICE_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2"
        )
)

data class Configuration(
        val clusterName: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
        val s3Url: String = config()[Key("S3_URL", stringType)],
        val s3Region: String = config()[Key("S3_REGION", stringType)],
        val s3AccessKey: String = config()[Key("S3_ACCESS_KEY", stringType)],
        val s3SecretKey: String = config()[Key("S3_SECRET_KEY", stringType)],
        val jwksUrl: JwkProvider = makeJwkProvider(config()[Key("SECURITY_TOKEN_SERVICE_JWKS_URL", stringType)]),
        val jwtIssuer: String = config()[Key("SECURITY_TOKEN_SERVICE_ISSUER", stringType)]
)

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties