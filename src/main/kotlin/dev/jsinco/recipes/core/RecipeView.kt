package dev.jsinco.recipes.core

import dev.jsinco.recipes.core.flaws.Flaw

data class RecipeView(val recipeIdentifier: String, val flaws: List<Flaw>)
