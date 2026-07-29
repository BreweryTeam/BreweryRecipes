package dev.jsinco.recipes.recipe.lore

import net.kyori.adventure.text.Component

interface LoreSection {
    fun type(): LoreType
    fun lore(indent: Boolean): List<Component>?
}
