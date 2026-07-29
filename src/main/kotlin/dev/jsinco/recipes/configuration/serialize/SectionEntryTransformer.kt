package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.gui.SectionEntry
import dev.jsinco.recipes.recipe.lore.LoreType
import eu.okaeri.configs.schema.GenericsPair
import eu.okaeri.configs.serdes.BidirectionalTransformer
import eu.okaeri.configs.serdes.SerdesContext

object SectionEntryTransformer : BidirectionalTransformer<String, SectionEntry>() {
    override fun getPair(): GenericsPair<String, SectionEntry> {
        return genericsPair(String::class.java, SectionEntry::class.java)
    }

    override fun leftToRight(
        data: String,
        serdesContext: SerdesContext
    ): SectionEntry? {
        return if (data.isBlank()) {
            SectionEntry(LoreType.SPACER)
        } else if (data.startsWith("+")) {
            val type = parseSectionType(data.substring(1).trim()) ?: return null
            SectionEntry(type, true)
        } else {
            val type = parseSectionType(data.trim()) ?: return null
            SectionEntry(type, false)
        }
    }

    private fun parseSectionType(name: String): LoreType? {
        val type = LoreType.entries.find { it.name.equals(name, ignoreCase = true) }
        return if (type != null && type != LoreType.SPACER) type else null
    }

    override fun rightToLeft(
        data: SectionEntry,
        serdesContext: SerdesContext
    ): String {
        return if (data.type == LoreType.SPACER) {
            ""
        } else if (data.indent) {
            "+" + data.type.name
        } else {
            data.type.name
        }
    }
}
