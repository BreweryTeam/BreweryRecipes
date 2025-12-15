package dev.jsinco.recipes.recipe.flaws.type

import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.recipe.flaws.FlawTextModifications
import net.kyori.adventure.text.Component

data class ReplacementFlawType(val replacement: String, val overwriteSpace: Boolean = false) : FlawType {

    override fun postProcess(text: String, pos: Int, config: FlawConfig): Component {
        return Component.text(text)
    }

    override fun findFlawModifications(
        component: Component,
        session: FlawType.ModificationFindSession
    ): FlawTextModifications {
        return FlawTextModificationWriter.randomPositionReplacement(component, session, 1.0, overwriteSpace) {
            replacement.repeat(it.length)
        }
    }

}