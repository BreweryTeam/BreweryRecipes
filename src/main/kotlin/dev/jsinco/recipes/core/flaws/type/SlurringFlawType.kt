package dev.jsinco.recipes.core.flaws.type

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.function.Predicate
import kotlin.math.roundToInt
import kotlin.random.Random

object SlurringFlawType : FlawType {
    override fun postProcess(
        text: String,
        pos: Int,
        config: FlawConfig
    ): Component {
        TODO("Not yet implemented")
    }

    override fun findFlawModifications(
        component: Component,
        config: FlawConfig,
        filter: Predicate<Int>
    ): FlawTextModifications {
        TODO("Not yet implemented")
    }

    override fun estimatedObscurationIntensity(intensity: Double): Double {
        TODO("Not yet implemented")
    }


}