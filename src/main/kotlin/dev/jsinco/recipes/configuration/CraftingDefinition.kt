package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.BreweryRecipes.Companion.key
import dev.jsinco.recipes.util.Logger
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

class CraftingDefinition : OkaeriConfig() {
    var enabled: Boolean = true
    var shaped: Boolean = false

    @Comment("Materials for a shapeless recipe (order does not matter)")
    var ingredients: List<Material> = listOf(Material.PAPER, Material.BOOK)

    @Comment("Row patterns for a shaped recipe (use spaces for empty slots, max 3 rows of 3)")
    var shape: List<String> = listOf("AB ", "   ", "   ")

    @Comment(
        "Maps each character in 'shape' to a Material name:",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html"
    )
    @CustomKey("ingredient-map")
    var ingredientMap: Map<String, Material> = mapOf("A" to Material.PAPER, "B" to Material.BOOK)

    fun register(item: ItemStack, keyValue: String) {
        val recipeKey = key(keyValue)
        Bukkit.removeRecipe(recipeKey)
        if (!this.enabled) return
        if (this.shaped) {
            val recipe = ShapedRecipe(recipeKey, item)
            val rows = this.shape.take(3).map { it.padEnd(3).take(3) }
            when (rows.size) {
                1 -> recipe.shape(rows[0])
                2 -> recipe.shape(rows[0], rows[1])
                else -> recipe.shape(rows[0], rows[1], rows[2])
            }
            this.ingredientMap.forEach { (char, material) ->
                recipe.setIngredient(char[0], material)
            }
            Bukkit.addRecipe(recipe)
            Logger.log("Added a shaped recipe with key $keyValue")
        } else {
            val recipe = ShapelessRecipe(recipeKey, item)
            this.ingredients.forEach { material ->
                recipe.addIngredient(material)
            }
            Bukkit.addRecipe(recipe)
            Logger.log("Added a shapeless recipe with key $keyValue")
        }
    }
}