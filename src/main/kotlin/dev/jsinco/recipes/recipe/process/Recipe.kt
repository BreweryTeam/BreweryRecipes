package dev.jsinco.recipes.recipe.process

import dev.jsinco.recipes.recipe.RecipeDisplay
import net.kyori.adventure.text.Component

data class Recipe(val recipeKey: String, val steps: List<Step>) : RecipeDisplay {
    override fun toLore(): List<Component> {
        TODO("Not yet implemented")
    }

    override fun displayName(brewDisplayName: Component): Component {
        TODO("Not yet implemented")
    }

    override fun scoreEquivalent(): Double {
        TODO("Not yet implemented")
    }
}
