package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.process.Step
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class RecipeView(
    val recipeIdentifier: String,
    flaws: List<Flaw>,
    val invertedReveals: List<Set<Int>>
) : RecipeDisplay {

    val flaws = flaws.subList(0, flaws.size.coerceAtMost(10))
    private var memoizedFragmentation: Double = Double.NaN
    private var memoizedFragmentationVersion: Int = Int.MIN_VALUE

    override fun recipeKey(): String = recipeIdentifier

    companion object {
        fun of(identifier: String, flaws: List<Flaw>): RecipeView {
            return RecipeView(identifier, flaws, listOf())
        }
    }

    fun fragmentation(): Double {
        val current = RecipeViewLoreWriter.version
        if (memoizedFragmentationVersion != current) {
            memoizedFragmentation = RecipeViewLoreWriter.estimateFragmentation(this)
            memoizedFragmentationVersion = current
        }
        return memoizedFragmentation
    }

    override fun fragmentationGroup() = FragmentationGroup.of(fragmentation())

    override fun toLore(): List<Component>? {
        return RecipeViewLoreWriter.writeLore(this, BreweryRecipes.brewingIntegration)
    }

    override fun displayName(brewDisplayName: Component): Component {
        val translationName = fragmentationGroup().translationKey
        return Component.translatable(translationName, Argument.component("name", brewDisplayName))
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }

    override fun scoreEquivalent(): Double {
        return 1 - fragmentation() / 100
    }

    override fun displaySteps(): List<Step>? = BreweryRecipes.brewingIntegration.getRecipe(recipeIdentifier)?.steps

    override fun generateView(): RecipeView = this

}
