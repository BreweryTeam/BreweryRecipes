package dev.jsinco.recipes.recipe

import net.kyori.adventure.text.Component

data class BreweryRecipeGroup(val id: String, val displayName: Component, val recipes: List<BreweryRecipe>)
