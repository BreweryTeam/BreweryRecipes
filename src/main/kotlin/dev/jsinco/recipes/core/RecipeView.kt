package dev.jsinco.recipes.core

import dev.jsinco.recipes.core.flaws.Flaw

data class RecipeView(val recipe: BreweryRecipe, val flaws: List<Flaw>)
