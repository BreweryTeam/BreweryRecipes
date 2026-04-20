package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.recipe.flaws.Flaw
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class RecipeView(
    val recipeIdentifier: String,
    flaws: List<Flaw>,
    val invertedReveals: List<Set<Int>>
) : RecipeDisplay {

    val flaws = flaws.subList(0, flaws.size.coerceAtMost(10))
    override fun recipeKey(): String = recipeIdentifier

    companion object {
        fun of(identifier: String, flaws: List<Flaw>): RecipeView {
            return RecipeView(identifier, flaws, listOf())
        }
    }

    override fun toLore(): List<Component>? {
        return RecipeViewLoreWriter.writeLore(this, Recipes.brewingIntegration)
    }

    override fun displayName(brewDisplayName: Component): Component {
        val fragmentation = RecipeViewLoreWriter.estimateFragmentation(this)
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
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }

    override fun scoreEquivalent(): Double {
        return 1 - RecipeViewLoreWriter.estimateFragmentation(this) / 100
    }

}
