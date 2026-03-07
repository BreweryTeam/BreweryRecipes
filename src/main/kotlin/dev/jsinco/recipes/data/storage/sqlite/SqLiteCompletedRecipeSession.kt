package dev.jsinco.recipes.data.storage.sqlite

import com.google.gson.JsonParser
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.data.serdes.Serdes
import dev.jsinco.recipes.data.serdes.StepSerdes
import dev.jsinco.recipes.data.storage.CompletedRecipeStorageSession
import dev.jsinco.recipes.data.storage.StorageSessionExecutor
import dev.jsinco.recipes.recipe.process.Recipe
import dev.jsinco.recipes.util.UuidUtil
import java.util.*
import java.util.concurrent.CompletableFuture

class SqLiteCompletedRecipeSession(private val storageSessionExecutor: StorageSessionExecutor) :
    CompletedRecipeStorageSession {
    override fun insertOrUpdateRecipeCompletion(
        playerUuid: UUID,
        recipe: Recipe
    ): CompletableFuture<Void?> {
        return storageSessionExecutor.runStatement(
            """
            INSERT OR REPLACE INTO completed_recipe
                  VALUES(?,?,?);
        """.trimIndent()
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            it.setString(2, recipe.recipeKey)
            it.setString(3, Serdes.serializeCollection(recipe.steps, StepSerdes::serializeStep).toString())
            return@runStatement null
        }
    }

    override fun removeRecipeCompletion(
        playerUuid: UUID,
        recipeKey: String
    ): CompletableFuture<Void?> {
        return storageSessionExecutor.runStatement(
            """
                DELETE FROM completed_recipe
                    WHERE player_uuid = ? AND recipe_key = ?;
            """.trimIndent()
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            it.setString(2, recipeKey)
            it.execute()
            return@runStatement null
        }
    }

    override fun selectRecipeCompletions(playerUuid: UUID): CompletableFuture<List<Recipe>?> {
        return storageSessionExecutor.runStatement(
            """
                SELECT recipe_key, steps FROM ${Recipes.Companion.recipesConfig.storage.mysql.prefix}completed_recipe
                    WHERE player_uuid = ?;
            """
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            val result = it.executeQuery()
            val output = mutableListOf<Recipe>()
            while (result.next()) {
                val steps = Serdes.deserializeList(
                    JsonParser.parseString(result.getString("steps")).asJsonArray,
                    StepSerdes::deserializeStep
                )
                output.add(Recipe(result.getString("recipe_key"), steps))
            }
            return@runStatement output
        }
    }
}