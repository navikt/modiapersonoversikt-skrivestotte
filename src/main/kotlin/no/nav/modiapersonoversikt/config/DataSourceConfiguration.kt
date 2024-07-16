package no.nav.modiapersonoversikt.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.modiapersonoversikt.log
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class DataSourceConfiguration(val env: Configuration) {
    private var dataSource = createDatasource("user")

    fun userDataSource() = dataSource

    fun runFlyway() {
        Flyway
            .configure()
            .dataSource(userDataSource())
            .load()
            .migrate()
    }

    private fun createDatasource(user: String): DataSource {
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

        return HikariDataSource(config)
    }
}
