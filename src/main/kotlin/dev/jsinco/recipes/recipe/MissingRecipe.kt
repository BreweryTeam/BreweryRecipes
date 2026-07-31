package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.process.Step
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class MissingRecipe(val recipeIdentifier: String) : RecipeDisplay {

    override fun recipeKey(): String = recipeIdentifier

    override fun toLore(): List<Component>? {
        return RecipeViewLoreWriter.writeLore(this, BreweryRecipes.brewingIntegration)
    }

    override fun displayName(brewDisplayName: Component): Component {
        return Component.translatable(
            "breweryrecipes.gui.recipes.name.missing",
            Argument.component("name", brewDisplayName)
        ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }

    override fun scoreEquivalent(): Double = 0.0

    override fun fragmentationGroup() = FragmentationGroup.MISSING

    override fun displaySteps(): List<Step>? = null

    override fun generateView(): RecipeView? = null

}
