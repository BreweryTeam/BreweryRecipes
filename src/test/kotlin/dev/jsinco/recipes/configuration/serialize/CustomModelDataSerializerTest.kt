package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.util.ColorUtil
import eu.okaeri.configs.configurer.InMemoryConfigurer
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerdesContext
import eu.okaeri.configs.serdes.SerializationData
import eu.okaeri.configs.serdes.standard.StandardSerdes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class CustomModelDataSerializerTest {

    private val configurer: InMemoryConfigurer = InMemoryConfigurer().apply {
        register(StandardSerdes())
        register(ColorSerializer)
    }
    private val serializer = CustomModelDataSerializer
    private val serdesContext = SerdesContext.of(configurer)

    @Test
    fun `supports CustomModelData class`() {
        assertEquals(true, serializer.supports(CustomModelData::class.java))
    }

    @Test
    fun `serialize expanded format`() {
        val customModelData = CustomModelData.customModelData()
            .addFloats(listOf(10f, 20.5f))
            .addFlags(listOf(true, false))
            .addStrings(listOf("a", "b"))
            .addColors(listOf(color("RED"), color("GREEN")))
            .build()
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(customModelData, serializationData, GenericsDeclaration.of(customModelData))
        val output = serializationData.asMap()

        assertEquals(listOf(10f, 20.5f), output["floats"])
        assertEquals(listOf(true, false), output["flags"])
        assertEquals(listOf("a", "b"), output["strings"])
        assertEquals(listOf("RED", "GREEN"), output["colors"])
    }

    @Test
    fun `deserialize expanded format`() {
        val deserializationData = DeserializationData(
            mapOf(
                "floats" to listOf(10f, 20.5f),
                "flags" to listOf(true, false),
                "strings" to listOf("a", "b"),
                "colors" to listOf("RED", "GREEN")
            ),
            configurer,
            serdesContext
        )
        val output = serializer.deserialize(deserializationData, GenericsDeclaration.of(CustomModelData::class.java))

        val expected = CustomModelData.customModelData()
            .addFloats(listOf(10f, 20.5f))
            .addFlags(listOf(true, false))
            .addStrings(listOf("a", "b"))
            .addColors(listOf(color("RED"), color("GREEN")))
            .build()
        assertEquals(expected, output)
    }

    @Test
    fun `serialize expanded format omits empty fields`() {
        val customModelData = CustomModelData.customModelData()
            .addFloats(listOf(10f, 20.5f))
            .addColors(listOf(color("RED"), color("GREEN")))
            .build()
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(customModelData, serializationData, GenericsDeclaration.of(customModelData))
        val output = serializationData.asMap()

        assertEquals(listOf(10f, 20.5f), output["floats"])
        assertNull(output["flags"])
        assertNull(output["strings"])
        assertEquals(listOf("RED", "GREEN"), output["colors"])
    }

    @Test
    fun `deserialize expanded format omits empty fields`() {
        val deserializationData = DeserializationData(
            mapOf(
                "floats" to listOf(10f, 20.5f),
                "colors" to listOf("RED", "GREEN")
            ),
            configurer,
            serdesContext
        )
        val output = serializer.deserialize(deserializationData, GenericsDeclaration.of(CustomModelData::class.java))

        val expected = CustomModelData.customModelData()
            .addFloats(listOf(10f, 20.5f))
            .addColors(listOf(color("RED"), color("GREEN")))
            .build()
        assertEquals(expected, output)
    }

    @Test
    fun `serialize compact format`() {
        val customModelData = CustomModelData.customModelData()
            .addFloats(listOf(10f))
            .addFlags(listOf(true))
            .addStrings(listOf("a"))
            .addColors(listOf(color("RED")))
            .build()
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(customModelData, serializationData, GenericsDeclaration.of(customModelData))
        val output = serializationData.asMap()

        assertEquals(10f, output["floats"])
        assertEquals(true, output["flags"])
        assertEquals("a", output["strings"])
        assertEquals("RED", output["colors"])
    }

    @Test
    fun `deserialize compact format`() {
        val deserializationData = DeserializationData(
            mapOf(
                "floats" to 10f,
                "flags" to true,
                "strings" to "a",
                "colors" to "RED"
            ),
            configurer,
            serdesContext
        )
        val output = serializer.deserialize(deserializationData, GenericsDeclaration.of(CustomModelData::class.java))

        val expected = CustomModelData.customModelData()
            .addFloats(listOf(10f))
            .addFlags(listOf(true))
            .addStrings(listOf("a"))
            .addColors(listOf(color("RED")))
            .build()
        assertEquals(expected, output)
    }

    @Test
    fun `serialize compact format omits empty fields`() {
        val customModelData = CustomModelData.customModelData()
            .addFlags(listOf(true))
            .addStrings(listOf("a"))
            .build()
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(customModelData, serializationData, GenericsDeclaration.of(customModelData))
        val output = serializationData.asMap()

        assertNull(output["floats"])
        assertEquals(true, output["flags"])
        assertEquals("a", output["strings"])
        assertNull(output["colors"])
    }

    @Test
    fun `deserialize compact format omits empty fields`() {
        val deserializationData = DeserializationData(
            mapOf(
                "flags" to true,
                "strings" to "a"
            ),
            configurer,
            serdesContext
        )
        val output = serializer.deserialize(deserializationData, GenericsDeclaration.of(CustomModelData::class.java))

        val expected = CustomModelData.customModelData()
            .addFlags(listOf(true))
            .addStrings(listOf("a"))
            .build()
        assertEquals(expected, output)
    }

    @Test
    fun `serialize legacy format`() {
        val customModelData = CustomModelData.customModelData()
            .addFloats(listOf(10f))
            .build()
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(customModelData, serializationData, GenericsDeclaration.of(customModelData))
        val output = serializationData.asMap()

        assertEquals(10, output[ObjectSerializer.VALUE])
        assertNull(output["floats"])
        assertNull(output["flags"])
        assertNull(output["strings"])
        assertNull(output["colors"])
    }

    @Test
    fun `deserialize legacy format`() {
        val deserializationData = DeserializationData(
            mapOf(ObjectSerializer.VALUE to 10),
            configurer,
            serdesContext
        )
        val output = serializer.deserialize(deserializationData, GenericsDeclaration.of(CustomModelData::class.java))

        val expected = CustomModelData.customModelData()
            .addFloats(listOf(10f))
            .build()
        assertEquals(expected, output)
    }

    @Test
    fun `serialize non-integer float is not legacy format`() {
        val customModelData = CustomModelData.customModelData()
            .addFloats(listOf(10.5f))
            .build()
        val serializationData = SerializationData(configurer, serdesContext)

        serializer.serialize(customModelData, serializationData, GenericsDeclaration.of(customModelData))
        val output = serializationData.asMap()

        assertEquals(10.5f, output["floats"])
        assertNull(output["flags"])
        assertNull(output["strings"])
        assertNull(output["colors"])
    }

    @Test
    fun `deserialize non-integer float is not legacy format but can parse anyway`() {
        val deserializationData = DeserializationData(
            mapOf(ObjectSerializer.VALUE to 10.5f),
            configurer,
            serdesContext
        )
        val output = serializer.deserialize(deserializationData, GenericsDeclaration.of(CustomModelData::class.java))

        val expected = CustomModelData.customModelData()
            .addFloats(listOf(10.5f))
            .build()
        assertEquals(expected, output)
    }

    private fun color(s: String) = ColorUtil.parseColorString(s)!!

}
