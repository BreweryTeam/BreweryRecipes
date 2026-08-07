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

    fun register(keyValue: String, item: ItemStack, oldRecipe: CraftingDefinition? = null) {
        val recipeKey = key(keyValue)
        // Can't compare Bukkit recipes directly as they don't implement equals()
        if (this == oldRecipe && item == Bukkit.getRecipe(recipeKey)?.result) return
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CraftingDefinition) return false

        if (enabled != other.enabled) return false
        if (shaped != other.shaped) return false
        if (ingredients != other.ingredients) return false
        if (shape != other.shape) return false
        if (ingredientMap != other.ingredientMap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + shaped.hashCode()
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + ingredientMap.hashCode()
        return result
    }

}