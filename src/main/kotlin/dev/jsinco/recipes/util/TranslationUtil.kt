package dev.jsinco.recipes.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import java.util.*

object TranslationUtil {

    fun render(component: Component): Component {
        return GlobalTranslator.render(component, Locale.ENGLISH)
    }
}