package dev.jsinco.recipes.recipe.flaws.type

import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawTextModifications
import net.kyori.adventure.text.Component
import java.util.function.Predicate

interface FlawType {

    fun postProcess(text: String, pos: Int, config: FlawConfig): Component

    fun findFlawModifications(component: Component, session: ModificationFindSession): FlawTextModifications

    data class ModificationFindSession(
        val stepIndex: Int,
        val config: FlawConfig,
        val filter: Predicate<Int>
    ) {

        fun appliesTo(pos: Int): Boolean {
            return config.extent.appliesTo(stepIndex, pos) && filter.test(pos)
        }
    }
}