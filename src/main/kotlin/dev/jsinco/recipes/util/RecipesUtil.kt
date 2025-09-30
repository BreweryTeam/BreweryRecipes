package dev.jsinco.recipes.util

import dev.jsinco.recipes.Recipes
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object RecipesUtil {
    val RECIPES_KEY = Recipes.key("recipes")!!
    val RECIPES_PDC_TYPE = PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING)


    fun hasRecipe(player: Player, recipeKey: String): Boolean {
        return player.persistentDataContainer.get(RECIPES_KEY, RECIPES_PDC_TYPE)
            ?.contains(recipeKey) ?: false
    }

    fun registerRecipe(player: Player, recipeKey: String) {
        val alreadyRegistered = player.persistentDataContainer.get(RECIPES_KEY, RECIPES_PDC_TYPE)
        if (alreadyRegistered?.contains(recipeKey) ?: false) {
            return
        }
        val newRecipes = (alreadyRegistered ?: listOf()) + listOf(recipeKey)
        player.persistentDataContainer.set(RECIPES_KEY, RECIPES_PDC_TYPE, newRecipes)
    }

    fun unregisterRecipe(player: Player, recipeKey: String) {
        val alreadyRegistered = player.persistentDataContainer.get(RECIPES_KEY, RECIPES_PDC_TYPE)
        if (alreadyRegistered?.contains(recipeKey) ?: false) {
            return
        }
        val newRecipes = (alreadyRegistered ?: listOf()) - listOf(recipeKey)
        player.persistentDataContainer.set(RECIPES_KEY, RECIPES_PDC_TYPE, newRecipes)
    }
}