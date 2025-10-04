package dev.jsinco.recipes.core.flaws.type

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.function.Predicate

object ObfuscationFlawType : FlawType {

    override fun postProcess(text: String, pos: Int, config: FlawConfig): Component {
        return Component.text(text)
            .decoration(TextDecoration.OBFUSCATED, true)
    }

    override fun findFlawModifications(
        component: Component,
        config: FlawConfig,
        filter: Predicate<Int>
    ): FlawTextModifications {
        return FlawTextModificationWriter.randomPositionReplacement(component, config, 1.0, filter) {
            it
        }
    }
}
