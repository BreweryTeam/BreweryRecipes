package dev.jsinco.recipes.core.flaws.type

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import net.kyori.adventure.text.Component
import java.util.function.Predicate

interface FlawType {

    fun postProcess(text: String, pos: Int, config: FlawConfig): Component

    fun findFlawModifications(component: Component, session: ModificationFindSession): FlawTextModifications

    fun estimatedObscurationIntensity(intensity: Double): Double

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