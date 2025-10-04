package dev.jsinco.recipes.core.flaws.type

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import net.kyori.adventure.text.Component
import java.util.function.Predicate

interface FlawType {

    fun postProcess(text: String, pos: Int, config: FlawConfig): Component

    fun findFlawModifications(component: Component, config: FlawConfig, filter: Predicate<Int>): FlawTextModifications
}