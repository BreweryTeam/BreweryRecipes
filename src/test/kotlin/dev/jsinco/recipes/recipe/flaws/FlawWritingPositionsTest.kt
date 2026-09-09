package dev.jsinco.recipes.recipe.flaws

import dev.jsinco.recipes.recipe.flaws.type.FlawType
import dev.jsinco.recipes.recipe.flaws.type.ObfuscationFlawType
import dev.jsinco.recipes.recipe.flaws.type.ReplacementFlawType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FlawWritingPositionsTest {

    private val lineWithMultiCharEmoji = "① Cook for 3 min 🔥"
    private val normalLoreLine = "② Distill 3 times ⚗"

    private class Written(val text: String, val obscured: Set<Int>, val replacements: Map<Int, String>)

    private fun mixedFlaws(seed: Int) = listOf(
        Flaw(ReplacementFlawType("  "), FlawConfig(FlawExtent.Everywhere, seed, 55.0)),
        Flaw(ReplacementFlawType(" "), FlawConfig(FlawExtent.Everywhere, seed + 31, 55.0)),
        Flaw(ObfuscationFlawType, FlawConfig(FlawExtent.Everywhere, seed + 71, 55.0))
    )

    private fun growingFlaws(seed: Int) = listOf(
        Flaw(ReplacementFlawType("  "), FlawConfig(FlawExtent.Everywhere, seed, 53.3)),
        Flaw(ReplacementFlawType("  "), FlawConfig(FlawExtent.Everywhere, seed + 17, 53.3)),
        Flaw(ReplacementFlawType("  "), FlawConfig(FlawExtent.Everywhere, seed + 53, 53.3))
    )

    // What RecipeViewLoreWriter does but without needing a running server
    private fun write(flaws: List<Flaw>, line: String = this.lineWithMultiCharEmoji): Written {
        val base = Component.text(line)
        val claimed = mutableListOf<Int>()
        val modifications = flaws.associateWith { flaw ->
            val session = FlawType.ModificationFindSession(0, flaw.config) { !claimed.contains(it) }
            flaw.type.findFlawModifications(base, session)
                .also { claimed.addAll(it.modifiedPoints.keys) }
        }
        var output: Component = base
        var offsets = mapOf<Int, Int>()
        val obscured = mutableSetOf<Int>()
        for (flaw in flaws) {
            val flawModifications = modifications[flaw] ?: continue
            output = FlawTextModificationWriter.process(output, flawModifications, flaw, offsets) {
                obscured.add(it)
            }
            offsets = flawModifications.offsets(offsets)
        }
        val replacements = obscured.associateWith { position ->
            modifications.values.firstNotNullOf { it.get(position) }
        }
        return Written(PlainTextComponentSerializer.plainText().serialize(output), obscured, replacements)
    }

    private fun assertWritesWhereItReports(flaws: List<Flaw>, line: String = this.lineWithMultiCharEmoji) {
        val written = write(flaws, line)
        val expected = line.indices.joinToString("") { position ->
            written.replacements[position] ?: line[position].toString()
        }
        assertEquals(expected, written.text, "The writer put its flaws somewhere else than it reported")
    }

    private fun hasBrokenMultiCharEmoji(text: String): Boolean {
        var index = 0
        while (index < text.length) {
            val character = text[index]
            if (character.isHighSurrogate()) {
                if (index + 1 >= text.length || !text[index + 1].isLowSurrogate()) return true
                index += 2
                continue
            }
            if (character.isLowSurrogate()) return true
            index++
        }
        return false
    }

    private fun flawsTheClosingSign(line: String, flaws: (Int) -> List<Flaw>): Boolean {
        val sign = line.offsetByCodePoints(line.length, -1)
        return (1..400).any { seed -> write(flaws(seed), line).obscured.contains(sign) }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 7, 42, 1966579771, -2099442340, 339756876, 214347846, -1179097754])
    fun writesWhereItReportsWithMixedFlaws(seed: Int) {
        assertWritesWhereItReports(mixedFlaws(seed))
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 7, 42, 835844324, 1247748511, -1964260792, 1479873046, 1405806353, 1903994396])
    fun writesWhereItReportsWithGrowingFlaws(seed: Int) {
        assertWritesWhereItReports(growingFlaws(seed))
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 7, 42, 835844324, 1247748511, -1964260792, 1479873046, 1903994396])
    fun writesWhereItReportsOnBasicPlaneSigns(seed: Int) {
        assertWritesWhereItReports(mixedFlaws(seed), normalLoreLine)
        assertWritesWhereItReports(growingFlaws(seed), normalLoreLine)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 7, 42, 835844324, 1479873046, -2099442340])
    fun neverTearsASurrogatePairApart(seed: Int) {
        for (flaws in listOf(mixedFlaws(seed), growingFlaws(seed))) {
            val rendered = write(flaws).text
            assertTrue(
                !hasBrokenMultiCharEmoji(rendered) && !rendered.contains('�'),
                "Seed $seed broke the emoji apart: $rendered"
            )
        }
    }

    @Test
    fun flawsEmojiOnBothPlanesAlike() {
        assertTrue(flawsTheClosingSign(lineWithMultiCharEmoji, ::growingFlaws), "The two char emoji is never flawed")
        assertTrue(flawsTheClosingSign(normalLoreLine, ::growingFlaws), "The one char emoji is never flawed")
        assertTrue(flawsTheClosingSign(lineWithMultiCharEmoji, ::mixedFlaws), "The two char emoji is never flawed")
        assertTrue(flawsTheClosingSign(normalLoreLine, ::mixedFlaws), "The one char emoji is never flawed")
    }
}
