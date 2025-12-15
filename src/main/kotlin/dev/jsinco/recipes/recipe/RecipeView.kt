package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.recipe.flaws.Flaw
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument

class RecipeView(
    val recipeIdentifier: String,
    flaws: List<Flaw>,
    val invertedReveals: List<Set<Int>>
) {

    val flaws = flaws.subList(0, flaws.size.coerceAtMost(10))

    companion object {
        fun of(identifier: String, flaws: List<Flaw>): RecipeView {
            return RecipeView(identifier, flaws, listOf())
        }
    }

    fun translation(brewDisplayName: Component): Component {
        val fragmentation = RecipeWriter.estimateFragmentation(this)
        val translationName = if (fragmentation == 0.0) {
            "recipes.display.recipe.name.complete"
        } else if (fragmentation < 25.0) {
            "recipes.display.recipe.name.slightly-fragmented"
        } else if (fragmentation < 50.0) {
            "recipes.display.recipe.name.moderately-fragmented"
        } else if (fragmentation < 75.0) {
            "recipes.display.recipe.name.heavily-fragmented"
        } else {
            "recipes.display.recipe.name.severely-fragmented"
        }
        return Component.translatable(translationName, Argument.component("name", brewDisplayName))
    }

}
