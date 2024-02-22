package no.nav.modiapersonoversikt.config

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
        val mountPath = env.database.vaultMountpath
        val config = HikariConfig()
        config.jdbcUrl = env.database.jdbcUrl
        config.minimumIdle = 0
        config.maximumPoolSize = 4
        config.connectionTimeout = 5000
        config.maxLifetime = 30000
        config.isAutoCommit = false

        log.info("Creating DataSource to: ${env.database.jdbcUrl}")

        if (env.clusterName == "local") {
            config.username = "test"
            config.password = "test"
            return HikariDataSource(config)
        }

        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
            config,
            mountPath,
            dbRole(env.database.databaseName, user)
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DataSourceConfiguration::class.java)
        private fun dbRole(dbName: String, user: String): String = "$dbName-$user"

        fun migrateDb(configuration: Configuration, dataSource: DataSource) {
            Flyway
                .configure()
                .dataSource(dataSource)
                .also {
                    if (dataSource is HikariDataSource && configuration.clusterName != "local") {
                        val dbUser = dbRole(configuration.database.databaseName, "admin")
                        it.initSql("SET ROLE '$dbUser'")
                    }
                }
                .load()
                .migrate()
        }
    }
}
