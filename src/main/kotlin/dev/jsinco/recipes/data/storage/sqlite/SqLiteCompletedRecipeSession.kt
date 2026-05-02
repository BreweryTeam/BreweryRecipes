package dev.jsinco.recipes.data.storage.sqlite

import com.google.gson.JsonParser
import dev.jsinco.recipes.data.serdes.Serdes
import dev.jsinco.recipes.data.serdes.StepSerdes
import dev.jsinco.recipes.data.storage.CompletedRecipeStorageSession
import dev.jsinco.recipes.data.storage.StorageSessionExecutor
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.util.UuidUtil
import java.util.*
import java.util.concurrent.CompletableFuture

class SqLiteCompletedRecipeSession(private val storageSessionExecutor: StorageSessionExecutor) :
    CompletedRecipeStorageSession {
    override fun insertOrUpdateRecipeCompletion(
        playerUuid: UUID,
        recipe: BreweryRecipe
    ): CompletableFuture<Void?> {
        return storageSessionExecutor.runStatement(
            """
            INSERT OR REPLACE INTO completed_recipe
                  VALUES(?,?,?);
        """.trimIndent()
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            it.setString(2, recipe.identifier)
            it.setString(3, Serdes.serializeCollection(recipe.steps, StepSerdes::serializeStep).toString())
            it.execute()
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

    override fun selectRecipeCompletions(playerUuid: UUID): CompletableFuture<List<BreweryRecipe>?> {
        return storageSessionExecutor.runStatement(
            """
                SELECT recipe_key, steps FROM completed_recipe
                    WHERE player_uuid = ?;
            """
        ) {
            it.setBytes(1, UuidUtil.toBytes(playerUuid))
            val result = it.executeQuery()
            val output = mutableListOf<BreweryRecipe>()
            while (result.next()) {
                val steps = Serdes.deserializeList(
                    JsonParser.parseString(result.getString("steps")).asJsonArray,
                    StepSerdes::deserializeStep
                )
                output.add(BreweryRecipe(result.getString("recipe_key"), steps, 0.0))
            }
            return@runStatement output
        }
    }
}