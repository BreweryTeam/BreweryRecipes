package dev.jsinco.recipes.util

import dev.jsinco.recipes.Recipes
import org.bukkit.NamespacedKey

object PdcKeys {
    val BOOK_KEY: NamespacedKey = Recipes.key("recipe-book")
    val RECIPE_KEY = Recipes.key("recipe")
    val FLAW_KEY = Recipes.key("flaw")
}