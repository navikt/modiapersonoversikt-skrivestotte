package no.nav.modiapersonoversikt

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.flywaydb.core.Flyway
import javax.sql.DataSource

fun userDataSource(): DataSource = createDatasource("user")
fun adminDataSource(): DataSource = createDatasource("admin")

fun h2DataSource(): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;"
//    config.jdbcUrl = "jdbc:h2:tcp://localhost:8090/./testdb"
    config.username = "sa"
    config.password = "sa"
    config.maximumPoolSize = 4
    config.minimumIdle = 1

    return HikariDataSource(config)
}

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

private fun dbRole(user: String): String = "modiapersonoversikt-skrivestotte-$user"

private fun createDatasource(user: String): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = ""
    config.maximumPoolSize = 4
    config.minimumIdle = 1
    val mountPath = "postgresql/preprod-fss"

    return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
            config,
            mountPath,
            dbRole(user)
    )
}