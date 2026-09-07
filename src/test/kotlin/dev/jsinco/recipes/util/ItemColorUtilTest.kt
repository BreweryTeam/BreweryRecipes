package dev.jsinco.recipes.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class ItemColorUtilTest {

    @Test
    fun `strips the namespace`() {
        assertEquals(ItemColorUtil.getHex("apple"), ItemColorUtil.getHex("minecraft:apple"))
    }

    @ParameterizedTest
    @CsvSource(
        "acacia_slab, acacia_planks",
        "oak_stairs, oak_planks",
        "cobblestone_wall, cobblestone",
        "bamboo_button, bamboo_planks",
        "black_carpet, black_wool",
        "oak_wood, oak_log",
        "smooth_stone_slab, smooth_stone",
        "glass_pane, glass"
    )
    fun `a variant borrows the color of what it is made of`(material: String, source: String) {
        val expected = ItemColorUtil.getHex(source)
        assertNotNull(expected, "$source is missing from item-colors.json")
        assertEquals(expected, ItemColorUtil.getHex(material))
    }

    @ParameterizedTest
    @CsvSource(
        "grass_block, grass_block_top",
        "piston, piston_top",
        "furnace, furnace_top",
        "clock, clock_00",
        "carrots, carrots_stage0",
        "sunflower, sunflower_front"
    )
    fun `a multi texture material resolves to its most representative face`(material: String, texture: String) {
        val expected = ItemColorUtil.getHex(texture)
        assertNotNull(expected, "$texture is missing from item-colors.json")
        assertEquals(expected, ItemColorUtil.getHex(material))
    }

    @ParameterizedTest
    @CsvSource(
        "melon, melon_seeds",
        "grass_block, grass_block_snow",
        "furnace, furnace_minecart",
        "piston, piston_top_sticky",
        "fire, firework_star_overlay"
    )
    fun `does not borrow from unrelated textures`(material: String, unrelatedTexture: String) {
        val unrelated = ItemColorUtil.getHex(unrelatedTexture)
        assertNotNull(unrelated, "$unrelatedTexture is missing from item-colors.json")
        assertNotEquals(unrelated, ItemColorUtil.getHex(material))
    }

    @Test
    fun `unknown material has no color`() {
        assertNull(ItemColorUtil.getHex("not_a_real_material"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["grass_block", "tall_grass", "large_fern", "fern", "short_grass", "water", "oak_leaves"])
    fun `biome tinted materials are not grayscale`(material: String) {
        val channels = channels(material)
        assertTrue(channels.max() - channels.min() > 20, "$material resolved to a grayscale mask")
    }

    @ParameterizedTest
    @ValueSource(strings = ["coal_block", "black_wool", "black_concrete", "black_carpet"])
    fun `dark materials stay dark`(material: String) {
        assertTrue(channels(material).sum() < 180, "$material resolved too bright")
    }

    @ParameterizedTest
    @ValueSource(strings = ["apple", "wheat", "sugar_cane", "cocoa_beans", "honey_bottle", "glass_bottle",
        "brewing_stand", "cauldron", "oak_slab", "stone_brick_stairs", "white_bed", "chest", "clock"])
    fun `common materials resolve`(material: String) {
        assertNotNull(ItemColorUtil.getHex(material), "$material has no color")
    }

    private fun channels(material: String): List<Int> {
        val hex = ItemColorUtil.getHex(material)
        assertNotNull(hex, "$material has no color")
        val rgb = hex!!.removePrefix("#").toInt(16)
        return listOf(rgb shr 16 and 0xff, rgb shr 8 and 0xff, rgb and 0xff)
    }
}
