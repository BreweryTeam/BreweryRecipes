package dev.jsinco.recipes.util

import org.bukkit.Color
import java.util.*

object ColorUtil {
    val NAME_TO_COLOR_MAP: Map<String, Color> = mapOf(
        "WHITE" to Color.WHITE,
        "SILVER" to Color.SILVER,
        "GRAY" to Color.GRAY,
        "BLACK" to Color.BLACK,
        "RED" to Color.RED,
        "MAROON" to Color.MAROON,
        "YELLOW" to Color.YELLOW,
        "OLIVE" to Color.OLIVE,
        "LIME" to Color.LIME,
        "GREEN" to Color.GREEN,
        "AQUA" to Color.AQUA,
        "TEAL" to Color.TEAL,
        "BLUE" to Color.BLUE,
        "NAVY" to Color.NAVY,
        "FUCHSIA" to Color.FUCHSIA,
        "PURPLE" to Color.PURPLE,
        "ORANGE" to Color.ORANGE,
        "PINK" to Color.FUCHSIA,
        "BRIGHT_GRAY" to Color.SILVER,
        "BRIGHT_RED" to Color.fromRGB(255, 0, 0),
        "DARK_RED" to Color.fromRGB(128, 0, 0),
    )

    fun parseColorString(hexOrValue: String): Color? {
        val standardized = hexOrValue.replace("&", "").replace("#", "").uppercase(Locale.getDefault())
        return NAME_TO_COLOR_MAP[standardized] ?: try {
            return Color.fromRGB(
                standardized.substring(0, 2).toInt(16),
                standardized.substring(2, 4).toInt(16),
                standardized.substring(4, 6).toInt(16)
            )
        } catch (_: Exception) {
            return null
        }
    }
}
