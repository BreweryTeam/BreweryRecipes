package dev.jsinco.recipes.data.storage.mysql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.data.StorageType
import dev.jsinco.recipes.data.storage.CompletedRecipeStorageSession
import dev.jsinco.recipes.data.storage.RecipeViewStorageSession
import dev.jsinco.recipes.data.storage.StorageImpl
import dev.jsinco.recipes.data.storage.StorageSessionExecutor
import dev.jsinco.recipes.util.Logger
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class MySQLStorageImpl : StorageImpl {

    private val dataSource: HikariDataSource = setupDataSource()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


    override fun getType(): StorageType = StorageType.MySQL

    override fun recipeViewSession(): RecipeViewStorageSession {
        return MySqlRecipeViewSession(StorageSessionExecutor(executor, dataSource::getConnection))
    }

    override fun completedRecipeSession(): CompletedRecipeStorageSession {
        return MySqlCompletedRecipeSession(StorageSessionExecutor(executor, dataSource::getConnection))
    }


    private fun setupDataSource(): HikariDataSource {
        val config = HikariConfig()
        val jdbcUrl =
            "jdbc:mysql://${Recipes.Companion.recipesConfig.storage.mysql.host}:${Recipes.Companion.recipesConfig.storage.mysql.port}/${Recipes.Companion.recipesConfig.storage.mysql.database}"
        config.jdbcUrl = jdbcUrl
        config.username = Recipes.Companion.recipesConfig.storage.mysql.user
        config.password = Recipes.Companion.recipesConfig.storage.mysql.password

        config.poolName = "MySQLPool"
        config.connectionTestQuery = "SELECT 1"

        config.maximumPoolSize = 10
        config.minimumIdle = 2
        config.idleTimeout = 600_000 // 10m
        config.maxLifetime = 1_800_000 // 30m
        config.connectionTimeout = 30_000 // 30s

        return HikariDataSource(config)
    }

    override fun createTables() {
        val createRecipeViewStatement = """
            CREATE TABLE IF NOT EXISTS ${Recipes.Companion.recipesConfig.storage.mysql.prefix}recipe_view (
              player_uuid BINARY(16) NOT NULL,
              recipe_key VARCHAR(255) NOT NULL, /* MySQL doesn't allow TEXT as PK */
              recipe_flaws JSON NOT NULL,
              inverted_reveals JSON NOT NULL,
              PRIMARY KEY (player_uuid, recipe_key)
            );
        """.trimIndent()
        val createRecipeHistoryStatement = """
            CREATE TABLE IF NOT EXISTS ${Recipes.Companion.recipesConfig.storage.mysql.prefix}completed_recipe(
              player_uuid BINARY(16) NOT NULL,
              recipe_key VARCHAR(255) NOT NULL,
              steps JSON NOT NULL,
              PRIMARY KEY (player_uuid, recipe_key)
            );
        """.trimIndent()
        try {
            dataSource.connection.use { conn ->
                conn.prepareStatement(createRecipeViewStatement).use(PreparedStatement::execute)
                conn.prepareStatement(createRecipeHistoryStatement).use(PreparedStatement::execute)
            }
        } catch (e: SQLException) {
            Logger.logErr(e)
        }
    }
}