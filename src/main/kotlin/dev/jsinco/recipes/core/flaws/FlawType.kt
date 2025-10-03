package dev.jsinco.recipes.core.flaws

import net.kyori.adventure.text.Component
import java.util.function.Predicate

interface FlawType {

    fun postProcess(text: String, pos: Int, seed: Int): Component

    fun findFlawModifications(component: Component, config: FlawConfig, filter: Predicate<Int>): FlawTextModifications
}