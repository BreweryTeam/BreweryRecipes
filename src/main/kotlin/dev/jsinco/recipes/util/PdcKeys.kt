package dev.jsinco.recipes.util

import dev.jsinco.recipes.BreweryRecipes
import org.bukkit.NamespacedKey

object PdcKeys {
    val BOOK_KEY: NamespacedKey = BreweryRecipes.key("recipe-book")
    val RECIPE_KEY = BreweryRecipes.key("recipe")
    val FLAW_KEY = BreweryRecipes.key("flaw")
}