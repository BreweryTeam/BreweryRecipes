package dev.jsinco.recipes.util

import dev.jsinco.recipes.BreweryRecipes
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator

object TranslationUtil {

    fun render(component: Component): Component {
        return GlobalTranslator.render(component, BreweryRecipes.recipesConfig.language)
    }
}