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
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class MySQLStorageImpl : StorageImpl {

    private val dataSource: HikariDataSource = setupDataSource()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


    override fun getType(): StorageType = StorageType.MySQL


    private fun setupDataSource(): HikariDataSource {
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

        return HikariDataSource(config)
    }

    override fun createTables() {
        val sql = """
            CREATE TABLE IF NOT EXISTS ${Recipes.recipesConfig.storage.mysql.prefix}recipe_view (
              player_uuid BINARY(16) NOT NULL,
              recipe_key VARCHAR(255) NOT NULL, /* MySQL doesn't allow TEXT as PK */
              recipe_flaws JSON NOT NULL,
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