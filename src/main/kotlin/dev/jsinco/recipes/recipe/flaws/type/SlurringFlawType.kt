package dev.jsinco.recipes.recipe.flaws.type

import com.google.gson.JsonParser
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.recipe.flaws.FlawTextModifications
import dev.jsinco.recipes.recipe.flaws.drunkentext.DrunkenTextReplacement
import dev.jsinco.recipes.data.serdes.DrunkenTextSerdes
import dev.jsinco.recipes.data.serdes.Serdes
import dev.jsinco.recipes.util.FileUtil
import net.kyori.adventure.text.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object SlurringFlawType : FlawType {

    private val drunkenReplacements = retrieveDrunkenReplacements()

    private fun retrieveDrunkenReplacements(): List<DrunkenTextReplacement> {
        val destinationFile = File(
            Recipes.instance.dataFolder,
            "locale/${Recipes.recipesConfig.language.toLanguageTag()}.drunk_text.json"
        )
        FileUtil.saveResourceIfExists("/locale/en-US.drunk_text.json", destinationFile, false)
        FileInputStream(destinationFile).use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                return Serdes.deserializeList(JsonParser.parseReader(reader).asJsonArray, DrunkenTextSerdes::deserialize)
            }
        }
    }

    override fun postProcess(
        text: String,
        pos: Int,
        config: FlawConfig
    ): Component {
        return Component.text(text)
    }

    override fun findFlawModifications(
        component: Component,
        session: FlawType.ModificationFindSession
    ): FlawTextModifications {
        val flawTextModifications = FlawTextModifications()
        val config = session.config
        FlawTextModificationWriter.traverse(component) { text, startPos ->
            val replacements = drunkenReplacements
            for (replacement in replacements) {
                val found = replacement.replacements(text, config.seed, startPos) { pos ->
                    session.appliesTo(pos)
                }
                found.forEach {
                    flawTextModifications.write(it.pos, it.replacement, replacement.faultLevel)
                }
            }
        }
        return flawTextModifications
    }

}