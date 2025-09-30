package dev.jsinco.recipes.data.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.data.StorageType
import dev.jsinco.recipes.util.Logger
import java.io.File
import java.sql.SQLException

class MySQLStorageImpl(private val dataFolder: File) : StorageImpl {

    override fun getType(): StorageType = StorageType.MySQL

    companion object {
        private var dataSource: HikariDataSource? = null
    }

    init {
        setupDataSource()
        createTable()
    }

    @Synchronized
    private fun setupDataSource() {
        if (dataSource != null && !dataSource!!.isClosed) return

        val config = HikariConfig()
        val jdbcUrl =
            "jdbc:mysql://${Recipes.recipesConfig.storage.mysql.host}:${Recipes.recipesConfig.storage.mysql.port}/${Recipes.recipesConfig.storage.mysql.database}"
        config.jdbcUrl = jdbcUrl
        config.username = Recipes.recipesConfig.storage.mysql.user
        config.password = Recipes.recipesConfig.storage.mysql.password

        config.poolName = "MySQLPool"
        config.connectionTestQuery = "SELECT 1"

        config.maximumPoolSize = 10
        config.minimumIdle = 2
        config.idleTimeout = 600_000 // 10m
        config.maxLifetime = 1_800_000 // 30m
        config.connectionTimeout = 30_000 // 30s

        dataSource = HikariDataSource(config)
    }

    @Synchronized
    private fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS ${Recipes.recipesConfig.storage.mysql.prefix}data (
                uuid VARCHAR(36) PRIMARY KEY,
                data DOUBLE
            )
        """.trimIndent()

        try {
            dataSource!!.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sql)
                }
            }
        } catch (e: SQLException) {
            Logger.logErr(e)
        }
    }

}