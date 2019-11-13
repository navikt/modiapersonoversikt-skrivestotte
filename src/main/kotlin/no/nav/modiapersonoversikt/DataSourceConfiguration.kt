package no.nav.modiapersonoversikt

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.sql.DataSource


class DataSourceConfiguration(val env: Configuration) {
    private var userDataSource = createDatasource("user")
    private var adminDataSource = createDatasource("admin")

    fun userDataSource() = userDataSource
    fun adminDataSource() = adminDataSource

    private fun createDatasource(user: String): DataSource {
        val mountPath = env.vaultMountpath
        val config = HikariConfig()
        config.jdbcUrl = env.jdbcUrl
        config.minimumIdle = 0
        config.maximumPoolSize = 4
        config.isAutoCommit = false

        log.info("Creating DataSource to: ${env.jdbcUrl}")

        if (env.clusterName == "local") {
            config.username = "sa"
            config.password = "sa"
            return HikariDataSource(config)
        }

        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
                config,
                mountPath,
                dbRole(user)
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DataSourceConfiguration::class.java)
        private fun dbRole(user: String): String = "modiapersonoversikt-skrivestotte-$user"

        fun migrateDb(dataSource: DataSource) {
            Flyway
                    .configure()
                    .dataSource(dataSource)
                    .also {
                        if (dataSource is HikariDataSource && !dataSource.jdbcUrl.contains(":h2:")) {
                            val dbUser = dbRole("admin")
                            it.initSql("SET ROLE '$dbUser'")
                        }
                    }
                    .load()
                    .migrate()
        }
    }
}



