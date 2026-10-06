package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.util.ColorUtil
import eu.okaeri.configs.configurer.InMemoryConfigurer
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import eu.okaeri.configs.serdes.SerdesContext
import eu.okaeri.configs.serdes.standard.StandardSerdes
import eu.okaeri.configs.schema.GenericsDeclaration
import org.bukkit.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class ColorSerializerTest {

    private val configurer: InMemoryConfigurer = InMemoryConfigurer().also {
        it.register(StandardSerdes())
    }
    private val serializer = ColorSerializer
    private val serdesContext = SerdesContext.of(configurer)

    @Test
    fun `supports Color class`() {
        assertEquals(true, serializer.supports(Color::class.java))
    }

    @ParameterizedTest
    @ValueSource(strings = ["WHITE", "SILVER", "GRAY", "BLACK"])
    fun `serializes named colors to their names`(name: String) {
        val color = ColorUtil.NAME_TO_COLOR_MAP[name]!!
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String::class.java))

        assertEquals(name, serializationData.asMap()[ObjectSerializer.VALUE])
    }

    @Test
    fun `serializes unnamed colors to hex format`() {
        val color = Color.fromRGB(123, 45, 67)
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String::class.java))

        assertEquals("#7b2d43", serializationData.asMap()[ObjectSerializer.VALUE])
    }

    @Test
    fun `serializes color with zero values correctly`() {
        val color = Color.fromRGB(0, 0, 0)
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String::class.java))

        assertEquals("BLACK", serializationData.asMap()[ObjectSerializer.VALUE])
    }

    @Test
    fun `serializes color with max values correctly`() {
        val color = Color.fromRGB(255, 255, 255)
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String::class.java))

        assertEquals("WHITE", serializationData.asMap()[ObjectSerializer.VALUE])
    }

    @ParameterizedTest
    @CsvSource("WHITE, WHITE", "RED, RED", "BLUE, BLUE", "GREEN, GREEN", "BLACK, BLACK", "YELLOW, YELLOW")
    fun `deserializes named colors correctly`(input: String, expectedName: String) {
        val deserializationData = DeserializationData(
            mapOf(ObjectSerializer.VALUE to input),
            configurer,
            serdesContext
        )

        val color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String::class.java))

        assertNotNull(color)
        assertEquals(ColorUtil.NAME_TO_COLOR_MAP[expectedName]!!.asRGB(), color!!.asRGB())
    }

    @ParameterizedTest
    @CsvSource("#FF0000, FF0000", "#00FF00, 00FF00", "#0000FF, 0000FF", "#7B2D43, 7B2D43", "#ABCDEF, ABCDEF")
    fun `deserializes hex colors correctly`(input: String, expectedHex: String) {
        val deserializationData = DeserializationData(
            mapOf(ObjectSerializer.VALUE to input),
            configurer,
            serdesContext
        )

        val color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String::class.java))

        assertNotNull(color)
        val expectedRgb = Integer.parseInt(expectedHex, 16)
        assertEquals(expectedRgb, color!!.asRGB())
    }

    @ParameterizedTest
    @ValueSource(strings = ["#ff0000", "#00ff00", "#0000ff", "#abcdef", "#ABCDEF", "FF0000", "00FF00"])
    fun `deserializes case insensitive hex colors`(input: String) {
        val deserializationData = DeserializationData(
            mapOf(ObjectSerializer.VALUE to input),
            configurer,
            serdesContext
        )

        val color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String::class.java))

        assertNotNull(color)
    }

    @Test
    fun `deserializes null value returns null`() {
        val deserializationData = DeserializationData(
            mapOf(ObjectSerializer.VALUE to null),
            configurer,
            serdesContext
        )

        val color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String::class.java))

        assertNull(color)
    }

    @Test
    fun `deserializes empty string returns null`() {
        val deserializationData = DeserializationData(
            mapOf("value" to ""),
            configurer,
            serdesContext
        )

        val color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String::class.java))

        assertNull(color)
    }

    @Test
    fun `deserializes invalid string returns null`() {
        val deserializationData = DeserializationData(
            mapOf("value" to "INVALID_COLOR"),
            configurer,
            serdesContext
        )

        val color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String::class.java))

        assertNull(color)
    }
}