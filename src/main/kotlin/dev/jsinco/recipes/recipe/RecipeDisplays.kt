package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.recipe.process.Step
import net.kyori.adventure.text.Component

class RecipeDisplays(val recipeKey: String, vararg val recipeDisplays: RecipeDisplay) : RecipeDisplay {
    override fun recipeKey(): String = recipeKey

    override fun toLore(): List<Component>? {
        recipeDisplays.find { it.scoreEquivalent() == 1.0 }?.let {
            return it.toLore()
        }
        return recipeDisplays
            .mapNotNull { it.toLore() }
            .flatMap {
                val output = it.toMutableList()
                output.add(Component.empty())
                output
            }.dropLast(1)
    }

    override fun displayName(brewDisplayName: Component): Component {
        val min = recipeDisplays.maxBy { it.scoreEquivalent() }
        return min.displayName(brewDisplayName)
    }

    override fun scoreEquivalent(): Double {
        return recipeDisplays.maxOf { it.scoreEquivalent() }
    }

    override fun displaySteps(): List<Step>? {
        throw NotImplementedError()
    }

    override fun generateView(): RecipeView? {
        throw NotImplementedError()
    }

}
