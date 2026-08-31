package dev.jsinco.recipes.recipe.lore

import net.kyori.adventure.text.Component

object SpacerSection : LoreSection {
    override fun type() = LoreType.SPACER

    override fun lore(indent: Boolean) = listOf(Component.empty())
}
