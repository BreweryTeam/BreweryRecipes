package dev.jsinco.recipes.data.storage.sqlite

import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import dev.jsinco.recipes.data.serdes.FlawSerdes
import dev.jsinco.recipes.data.serdes.Serdes
import dev.jsinco.recipes.data.storage.RecipeViewStorageSession
import dev.jsinco.recipes.data.storage.StorageSessionExecutor
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.util.UuidUtil
import java.util.*
import java.util.concurrent.CompletableFuture

class SqLiteRecipeViewSession(private val storageSessionExecutor: StorageSessionExecutor) : RecipeViewStorageSession {

    override fun insertOrUpdateRecipeView(
        playerUuid: UUID,
        recipeView: RecipeView
    ): CompletableFuture<Void?> {
        return storageSessionExecutor.runStatement(
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
        return storageSessionExecutor.runStatement(
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
        return storageSessionExecutor.runStatement(
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
}