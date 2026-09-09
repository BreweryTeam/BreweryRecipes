package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.process.Step
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class RecipeView(
    val recipeIdentifier: String,
    flaws: List<Flaw> = emptyList(),
    val invertedReveals: List<Set<Int>> = emptyList()
) : RecipeDisplay {

    val flaws = flaws.subList(0, flaws.size.coerceAtMost(10))
    private var memoizedFragmentation: Double = Double.NaN
    private var memoizedFragmentationVersion: Int = Int.MIN_VALUE

    override fun recipeKey(): String = recipeIdentifier

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecipeView) return false

        if (recipeIdentifier != other.recipeIdentifier) return false
        if (flaws != other.flaws) return false
        if (invertedReveals != other.invertedReveals) return false

        return true
    }

    override fun hashCode(): Int {
        var result = recipeIdentifier.hashCode()
        result = 31 * result + flaws.hashCode()
        result = 31 * result + invertedReveals.hashCode()
        return result
    }

    override fun toString(): String {
        return "RecipeView(recipeIdentifier='$recipeIdentifier', invertedReveals=$invertedReveals, flaws=$flaws)"
    }

}
