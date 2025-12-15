package dev.jsinco.recipes.recipe.flaws

import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class FlawTextModificationWriterTest {
    @ParameterizedTest
    @CsvFileSource(resources = ["/component_traversal.csv"], delimiterString = "@")
    fun traverse(input: String, traversedString: String) {
        val inputComponent = JSONComponentSerializer.json().deserialize(input)
        var result = ""
        FlawTextModificationWriter.traverse(inputComponent) { text, startPos ->
            assertEquals(startPos, result.length, "Expected starting position to be equal to result length")
            result += text
        }
        assertEquals(result, traversedString)
    }

}