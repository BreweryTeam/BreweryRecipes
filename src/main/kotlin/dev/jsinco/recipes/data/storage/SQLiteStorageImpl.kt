package dev.jsinco.recipes.data.storage

import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jsinco.recipes.recipe.RecipeView
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
              recipe_flaws JSON NOT NULL,
              inverted_reveals JSON NOT NULL,
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
        return runStatement(
            """
                INSERT OR REPLACE INTO recipe_view
                  VALUES(?,?,?,?);
            """
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            it.setString(2, recipeView.recipeIdentifier)
            it.setString(3, Serdes.serializeCollection(recipeView.flaws, FlawSerdes::serializeFlaw).toString())
            it.setString(4, Serdes.serializeCollection(recipeView.invertedReveals) { ints ->
                Serdes.serializeCollection(ints) { number ->
                    JsonPrimitive(number)
                }
            }.toString())
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

    override fun selectRecipeViews(playerUuid: UUID): CompletableFuture<List<RecipeView>?> {
        return runStatement(
            """
                SELECT recipe_key, recipe_flaws, inverted_reveals FROM recipe_view
                    WHERE player_uuid = ?;
            """.trimIndent()
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            val result = it.executeQuery()
            val output = mutableListOf<RecipeView>()
            while (result.next()) {
                val flaws = Serdes.deserializeList(
                    JsonParser.parseString(result.getString("recipe_flaws")).asJsonArray,
                    FlawSerdes::deserializeFlaw
                )
                val recipeView = RecipeView(
                    result.getString("recipe_key"),
                    flaws,
                    Serdes.deserializeList(JsonParser.parseString(result.getString("inverted_reveals")).asJsonArray) { jsonArray ->
                        Serdes.deserializeSet(jsonArray.asJsonArray) { element ->
                            element.asInt
                        }
                    }
                )
                output.add(recipeView)
                // Replace views that were previously allowed to have infinite flaws
                if (flaws.size > 10) {
                    insertOrUpdateRecipeView(playerUuid, recipeView)
                }
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