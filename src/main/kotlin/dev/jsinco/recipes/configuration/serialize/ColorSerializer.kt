package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.util.ColorUtil
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import org.bukkit.Color
import java.util.HexFormat

object ColorSerializer : ObjectSerializer<Color> {
    private val EXPLICIT_HEX_FORMAT: HexFormat = HexFormat.of().withPrefix("#")

    override fun supports(type: Class<*>): Boolean {
        return Color::class.java.isAssignableFrom(type)
    }

    override fun serialize(`object`: Color, data: SerializationData, generics: GenericsDeclaration) {
        for ((name, color) in ColorUtil.NAME_TO_COLOR_MAP) {
            if (`object`.asRGB() == color.asRGB()) {
                data.setValue(name)
                return
            }
        }
        data.setValue("#" + EXPLICIT_HEX_FORMAT.toHexDigits(`object`.asRGB().toLong(), 6))
    }

    override fun deserialize(data: DeserializationData, generics: GenericsDeclaration): Color? {
        return ColorUtil.parseColorString(data.getValue(String::class.java) ?: return null)
    }
}
