package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import java.util.function.Predicate

object OmissionFlawType : FlawType {

    override fun postProcess(text: String, pos: Int, seed: Int): Component {
        return Component.text(text)
    }

    override fun findFlawModifications(
        component: Component,
        config: FlawConfig,
        filter: Predicate<Int>
    ): FlawTextModifications {
        return FlawTextModificationWriter.randomPositionReplacement(component, config, 1.0, filter) {
            " ".repeat(it.length)
        }
    }

}