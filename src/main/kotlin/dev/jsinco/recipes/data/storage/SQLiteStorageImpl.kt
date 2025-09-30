package dev.jsinco.recipes.data.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.data.StorageType
import dev.jsinco.recipes.util.Logger
import java.io.File
import java.sql.SQLException

class SQLiteStorageImpl(private val dataFolder: File) : StorageImpl {

    override fun getType(): StorageType = StorageType.SQLite

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
        val databaseFile = File(dataFolder, "bodyHealth.sqlite")
        val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"

        config.jdbcUrl = jdbcUrl
        config.poolName = "SQLitePool"
        config.connectionTestQuery = "SELECT 1"

        config.maximumPoolSize = 1 // SQLite handles only one write at a time
        config.minimumIdle = 1

        config.maxLifetime = 300_000 // 5m
        config.initializationFailTimeout = -1

        dataSource = HikariDataSource(config)
    }

    @Synchronized
    private fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS data (
                uuid TEXT PRIMARY KEY,
                data REAL
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