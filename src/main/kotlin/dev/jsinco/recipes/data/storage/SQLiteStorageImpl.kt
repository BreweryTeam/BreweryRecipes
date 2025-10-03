package dev.jsinco.recipes.data.storage

import com.google.gson.JsonParser
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.RecipeWriter
import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.data.StorageType
import dev.jsinco.recipes.data.serdes.FlawSerdes
import dev.jsinco.recipes.data.serdes.Serdes
import dev.jsinco.recipes.util.Logger
import dev.jsinco.recipes.util.UuidUtil
import java.io.File
import java.sql.PreparedStatement
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
    ): CompletableFuture<Void?> {
        val normalizedView = RecipeWriter.normalizeFlawsIfLowFragmentation(recipeView)
        return runStatement(
            """
                INSERT OR REPLACE INTO recipe_view
                  VALUES(?,?,?);
            """
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            it.setString(2, normalizedView.recipeIdentifier)
            it.setString(3, Serdes.serialize(normalizedView.flaws, FlawSerdes::serializeFlawBundle).toString())
            it.execute()
            return@runStatement null
        }
    }

    override fun removeRecipeView(
        playerUuid: UUID,
        recipeKey: String
    ): CompletableFuture<Void?> {
        return runStatement(
            """
                DELETE FROM recipe_view
                    WHERE player_uuid = ? AND recipe_key = ?;
            """
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            it.setString(2, recipeKey)
            it.execute()
            return@runStatement null
        }
    }

    override fun selectAllRecipeViews(): CompletableFuture<Map<UUID, MutableList<RecipeView>>?> {
        return runStatement(
            """
                SELECT * FROM recipe_view;
            """.trimIndent()
        ) {
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
                            FlawSerdes::deserializeFlawBundle
                        )
                    )
                )
            }
            return@runStatement output
        }
    }

    private fun <T> runStatement(statement: String, supplier: Function1<PreparedStatement, T>): CompletableFuture<T?> {
        return CompletableFuture.supplyAsync<T>({
            dataSource.connection.use {
                it.prepareStatement(statement).use { preparedStatement ->
                    return@supplyAsync supplier.invoke(preparedStatement)
                }
            }
        }, executor)
            .handleAsync { t, e ->
                if (e != null) {
                    Logger.logErr(e)
                    return@handleAsync null
                }
                return@handleAsync t
            }
    }
}