package dev.jsinco.recipes.util

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.gui.GuiItem
import kotlin.collections.iterator

object GUIUtil {

    fun openSlots(): List<Int> {
        val output = (0..<54).toMutableList()
        for (borderEntry in BreweryRecipes.guiConfig.borders) {
            output.removeAll(borderEntry.key.positions.toList())
        }
        for (guiOverride in BreweryRecipes.guiConfig.overrides) {
            for (slot in getValidSlots(guiOverride.pos)) {
                output.remove(slot)
            }
        }
        return output.toList()
    }

    fun getValidSlots(pos: String): List<Int> {
        return pos.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
    }

    fun borders(): List<Pair<GuiItem, Int>> {
        val list = mutableListOf<Pair<GuiItem, Int>>()
        for (borderEntry in BreweryRecipes.guiConfig.borders) {
            val borderType = borderEntry.key
            val palette = borderEntry.value
            for ((i, element) in borderType.positions.withIndex()) {
                val pos = element
                val item = palette.content[i % palette.content.size].generateItem()
                list.add(GuiItem(item, GuiItem.Type.NO_ACTION) to pos)
            }
        }
        return list
    }

}