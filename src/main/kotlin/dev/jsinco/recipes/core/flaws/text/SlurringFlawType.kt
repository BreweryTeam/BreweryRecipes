package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import java.util.function.Predicate

object SlurringFlawType : FlawType {

    override fun postProcess(
        text: String,
        pos: Int,
        seed: Int
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

}