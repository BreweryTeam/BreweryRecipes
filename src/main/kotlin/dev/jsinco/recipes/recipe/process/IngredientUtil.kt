package dev.jsinco.recipes.recipe.process

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object IngredientUtil {



    fun compileIngredients(ingredients: Map<Ingredient, Int>): Component {
        return ingredients.entries.stream()
            .map { entry ->
                Component.text(entry.value).color(NamedTextColor.GOLD).appendSpace().append(
                    entry.key.displayName // A Component, not supported
                ).colorIfAbsent(NamedTextColor.GRAY)
            }.collect(Component.toComponent(Component.text(", ")))
    }
}