package dev.jsinco.recipes.core

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.Flaw
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument

class RecipeView(val recipeIdentifier: String, val flaws: List<Flaw>) {

    var steps: Int? = null

    fun translation(brewDisplayName: Component): Component {
        val fragmentation = fragmentationLevel()
        val translationName = if (fragmentation == 0.0) {
            "recipes.display.recipe.name.complete"
        } else if (fragmentation < 25.0) {
            "recipes.display.recipe.name.somewhat-fragmented"
        } else if (fragmentation < 50.0) {
            "recipes.display.recipe.name.fragmented"
        } else {
            "recipes.display.recipe.name.heavily-fragmented"
        }
        return Component.translatable(translationName, Argument.component("name", brewDisplayName))
    }

    private fun fragmentationLevel(): Double {
        if (steps == null) {
            steps = Recipes.recipes()[recipeIdentifier]?.steps?.size
        }
        steps ?: return 100.0
        val temp = steps!!
        val fragmentation = flaws.map { it.extent.obscurationLevel(temp) * it.type.intensity() / 100 }
        return fragmentation.sum() * 100
    }
}
