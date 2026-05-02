package dev.jsinco.recipes.gui

import java.util.Locale

enum class RecipeBookMode {
    FRAGMENTS,
    BREWED;

    fun identifier() = name.lowercase(Locale.ROOT)

    fun next(): RecipeBookMode = entries[(ordinal + 1) % entries.size]
}
