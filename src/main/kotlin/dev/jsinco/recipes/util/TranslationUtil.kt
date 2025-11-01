package dev.jsinco.recipes.util

import dev.jsinco.recipes.Recipes
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator

object TranslationUtil {

    fun render(component: Component): Component {
        return GlobalTranslator.render(component, Recipes.recipesConfig.language)
    }
}