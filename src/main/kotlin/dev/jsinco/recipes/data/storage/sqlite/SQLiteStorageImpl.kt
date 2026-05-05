package dev.jsinco.recipes.data.storage.sqlite

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.data.StorageType
import dev.jsinco.recipes.data.storage.CompletedRecipeStorageSession
import dev.jsinco.recipes.data.storage.RecipeViewStorageSession
import dev.jsinco.recipes.data.storage.StorageImpl
import dev.jsinco.recipes.data.storage.StorageSessionExecutor
import dev.jsinco.recipes.util.Logger
import java.io.File
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class SQLiteStorageImpl(private val dataFolder: File) : StorageImpl {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val dataSource: HikariDataSource = setupDataSource()
    override fun getType(): StorageType = StorageType.SQLite

    override fun recipeViewSession(): RecipeViewStorageSession {
        return SqLiteRecipeViewSession(StorageSessionExecutor(executor, dataSource::getConnection))
    }

    override fun completedRecipeSession(): CompletedRecipeStorageSession {
        return SqLiteCompletedRecipeSession(StorageSessionExecutor(executor, dataSource::getConnection))
    }

    private fun setupDataSource(): HikariDataSource {
        val config = HikariConfig()
        val databaseFile = File(dataFolder, "breweryrecipes.sqlite")
        val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"

        config.jdbcUrl = jdbcUrl
        config.poolName = "SQLitePool"
        config.connectionTestQuery = "SELECT 1"

        config.maximumPoolSize = 1 // SQLite handles only one write at a time
        config.minimumIdle = 1

        config.maxLifetime = 300_000 // 5m
        config.initializationFailTimeout = -1

        return HikariDataSource(config)
    }

    override fun createTables() {
        val createRecipeViewStatement = """
            CREATE TABLE IF NOT EXISTS recipe_view (
              player_uuid BINARY(16) NOT NULL,
              recipe_key TEXT NOT NULL,
              recipe_flaws JSON NOT NULL,
              inverted_reveals JSON NOT NULL,
              PRIMARY KEY (player_uuid, recipe_key)
            );
        """.trimIndent()
        val createRecipeHistoryStatement = """
            CREATE TABLE IF NOT EXISTS completed_recipe(
              player_uuid BINARY(16) NOT NULL,
              recipe_key VARCHAR(255) NOT NULL,
              steps JSON NOT NULL,
              PRIMARY KEY (player_uuid, recipe_key)
            );
        """.trimIndent()
        try {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(createRecipeViewStatement)
                    stmt.execute(createRecipeHistoryStatement)
                }
            }
        } catch (e: SQLException) {
            Logger.logErr(e)
        }
        try {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("ALTER TABLE completed_recipe ADD COLUMN score REAL NOT NULL DEFAULT 0")
                }
            }
        } catch (_: SQLException) {
            // column already exists
        }
    }
}