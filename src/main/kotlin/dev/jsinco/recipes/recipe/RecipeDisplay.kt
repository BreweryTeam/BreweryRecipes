package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.recipe.process.Step
import net.kyori.adventure.text.Component

interface RecipeDisplay {

    fun recipeKey(): String

    fun toLore(): List<Component>?

    fun displayName(brewDisplayName: Component): Component

    fun scoreEquivalent(): Double

    fun fragmentationGroup(): FragmentationGroup

    fun displaySteps(): List<Step>?

    fun generateView(): RecipeView?

}
