package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import dev.jsinco.recipes.recipe.RecipeView

object RecipeViewSerdes {

    fun serializeRecipeView(recipeView: RecipeView): JsonElement {
        val output = JsonObject()
        output.addProperty("recipe", recipeView.recipeIdentifier)
        output.add("flaws", Serdes.serializeCollection(recipeView.flaws, FlawSerdes::serializeFlaw))
        output.add("inverted-reveals", Serdes.serializeCollection(recipeView.invertedReveals) { ints ->
            Serdes.serializeCollection(ints) { number -> JsonPrimitive(number) }
        })
        return output
    }

    fun deserializeRecipeView(json: JsonElement): RecipeView? {
        if (json !is JsonObject) return null
        val identifier = json.get("recipe")?.takeIf { it.isJsonPrimitive }?.asString ?: return null
        val flaws = json.getAsJsonArray("flaws")
            ?.let { Serdes.deserializeList(it, FlawSerdes::deserializeFlaw) }
            ?: listOf()
        val invertedReveals = json.getAsJsonArray("inverted-reveals")
            ?.let { array ->
                Serdes.deserializeList(array) { element ->
                    Serdes.deserializeSet(element.asJsonArray) { number -> number.asInt }
                }
            } ?: listOf()
        return RecipeView(identifier, flaws, invertedReveals)
    }
}
