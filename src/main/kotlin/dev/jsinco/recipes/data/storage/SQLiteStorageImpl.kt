package dev.jsinco.recipes.data.storage

import com.google.gson.JsonParser
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.data.StorageType
import dev.jsinco.recipes.data.serdes.FlawSerdes
import dev.jsinco.recipes.data.serdes.Serdes
import dev.jsinco.recipes.util.Logger
import dev.jsinco.recipes.util.UuidUtil
import java.io.File
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class SQLiteStorageImpl(private val dataFolder: File) : StorageImpl {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val dataSource: HikariDataSource = setupDataSource()
    override fun getType(): StorageType = StorageType.SQLite

    private fun setupDataSource(): HikariDataSource {
        val config = HikariConfig()
        val databaseFile = File(dataFolder, "recipes.sqlite")
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
        val sql = """
            CREATE TABLE IF NOT EXISTS recipe_view (
              player_uuid BINARY(16) NOT NULL,
              recipe_key TEXT NOT NULL,
              recipe_flaws TEXT NOT NULL,
              PRIMARY KEY (player_uuid, recipe_key)
            );
        """.trimIndent()

        try {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sql)
                }
            }
        } catch (e: SQLException) {
            Logger.logErr(e)
        }
    }
    
    override fun insertOrUpdateRecipeView(
        playerUuid: UUID,
        recipeView: RecipeView
    ): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync({
            dataSource.connection.prepareStatement(
                """
                INSERT OR REPLACE INTO ${Recipes.recipesConfig.storage.mysql.prefix}recipe_view
                  VALUES(?,?,?);
            """.trimIndent()
            ).use {
                it.setBytes(1, UuidUtil.toBytes(playerUuid))
                it.setString(2, recipeView.recipeIdentifier)
                it.setString(3, Serdes.serialize(recipeView.flaws, FlawSerdes::serialize).toString())
                it.execute()
            }
            return@supplyAsync null
        }, executor)
    }

    override fun removeRecipeView(
        playerUuid: UUID,
        recipeKey: String
    ): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync({
            dataSource.connection.prepareStatement(
                """
                DELETE FROM ${Recipes.recipesConfig.storage.mysql.prefix}recipe_view
                    WHERE player_uuid = ? AND recipe_key = ?;
            """.trimIndent()
            ).use {
                it.setBytes(1, UuidUtil.toBytes(playerUuid))
                it.setString(2, recipeKey)
                it.execute()
            }
            return@supplyAsync null
        }, executor)
    }

    override fun selectAllRecipeViews(): CompletableFuture<Map<UUID, List<RecipeView>>> {
        return CompletableFuture.supplyAsync({
            dataSource.connection.prepareStatement(
                """
                SELECT * FROM ${Recipes.recipesConfig.storage.mysql.prefix}recipe_view;
            """.trimIndent()
            ).use {
                val result = it.executeQuery()
                val output = mutableMapOf<UUID, MutableList<RecipeView>>()
                while (result.next()) {
                    val recipeViews = output.computeIfAbsent(UuidUtil.asUuid(result.getBytes("player_uuid"))) {
                        mutableListOf()
                    }
                    recipeViews.add(
                        RecipeView(
                            result.getString("recipe_key"),
                            Serdes.deserialize(
                                JsonParser.parseString(result.getString("recipe_flaws")).asJsonArray,
                                FlawSerdes::deserialize
                            )
                        )
                    )
                }
                return@supplyAsync output
            }
        }, executor)
    }
}