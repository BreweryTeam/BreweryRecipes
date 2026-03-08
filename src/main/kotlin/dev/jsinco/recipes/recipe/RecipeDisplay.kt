package dev.jsinco.recipes.recipe

import net.kyori.adventure.text.Component

interface RecipeDisplay {

    fun toLore(): List<Component>

    fun displayName(brewDisplayName: Component): Component

    fun scoreEquivalent() : Double
}