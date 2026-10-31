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
        data.setValue(ColorUtil.colorToString(`object`))
    }

    override fun deserialize(data: DeserializationData, generics: GenericsDeclaration): Color? {
        return ColorUtil.parseColorString(data.getValue(String::class.java) ?: return null)
    }
}
