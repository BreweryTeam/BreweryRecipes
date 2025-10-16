package dev.jsinco.recipes.util

object GUIUtil {

    fun getValidSlots(pos: String): List<Int> {
        return pos.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
    }

}