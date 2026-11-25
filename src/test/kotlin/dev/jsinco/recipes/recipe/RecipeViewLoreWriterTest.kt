package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.GuiConfig
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.configuration.RecipesTranslator
import dev.jsinco.recipes.integration.MockIntegration
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter.defragmentUntil
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawExtent
import dev.jsinco.recipes.recipe.flaws.type.ObfuscationFlawType
import dev.jsinco.recipes.recipe.process.Ingredient
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

class RecipeViewLoreWriterTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val integration = MockIntegration()

            val recipe = BreweryRecipe.Builder("recipeId")
                .mix(3600, "WATER", mapOf(
                    Ingredient("wheat", Component.text("Wheat")) to 1
                ))
                .distill(3)
                .age(2, "OAK")
                .difficulty(1.0)
                .build()
            integration.registerRecipe(recipe)

            val emptyRecipe = BreweryRecipe.Builder("emptyRecipe").build()
            integration.registerRecipe(emptyRecipe)

            BreweryRecipes.brewingIntegration = integration

            val recipesConfig = RecipesConfig()
            BreweryRecipes.recipesConfig = recipesConfig
            val guiConfig = GuiConfig()
            BreweryRecipes.guiConfig = guiConfig

            val dataFolder = Files.createTempDirectory("breweryrecipes").toFile()
            RecipesTranslator(File(dataFolder, "locale"), recipesConfig.language).also {
                it.reload()
                GlobalTranslator.translator().addSource(it)
            }
        }
    }

    private fun createFlaw(intensity: Double): Flaw {
        return Flaw(
            ObfuscationFlawType,
            FlawConfig(FlawExtent.Everywhere, 0, intensity)
        )
    }

    @Test
    fun `defragmentUntil returns view unchanged when fragmentation already below threshold`() {
        val initialView = RecipeView("recipeId",
            flaws = listOf(createFlaw(10.0)),
        )
        assertLTE(initialView.fragmentation(), 50.0)

        val threshold = 50.0
        val result = defragmentUntil(initialView, threshold, Random(0))
        assertLTE(initialView.fragmentation(), 50.0)

        assertEquals(initialView, result)
    }

    @Test
    fun `defragmentUntil returns view unchanged if threshold is 100`() {
        val view = RecipeView("recipeId",
            flaws = listOf(createFlaw(80.0)),
            invertedReveals = emptyList()
        )

        val threshold = 100.0
        val result = defragmentUntil(view, threshold, Random(0))
        assertEquals(view, result)
    }

    @Test
    fun `defragmentUntil empty recipe returns view unchanged`() {
        val view = RecipeView("emptyRecipe",
            flaws = listOf(createFlaw(100.0))
        )

        val threshold = 50.0
        val result = defragmentUntil(view, threshold, Random(0))
        assertEquals(view, result)
    }

    @Test
    fun `defragmentUntil reduces fragmentation below threshold`() {
        val view = RecipeView("recipeId",
            flaws = listOf(createFlaw(70.0)),
            invertedReveals = emptyList()
        )

        val threshold = 50.0
        val result = defragmentUntil(view, threshold, Random(0))
        assertLTE(result.fragmentation(), threshold)
    }

    @Test
    fun `defragmentUntil can reduce fragmentation to 0`() {
        val view = RecipeView("recipeId",
            flaws = listOf(createFlaw(100.0)),
            invertedReveals = emptyList()
        )

        val threshold = 0.0
        val result = defragmentUntil(view, threshold, Random(0))
        assertLTE(result.fragmentation(), threshold)
    }

    @Test
    fun `defragmentUntil can be called on a defragmented view`() {
        val view = RecipeView("recipeId",
            flaws = listOf(createFlaw(80.0)),
            invertedReveals = emptyList()
        )

        val threshold = 60.0
        val result = defragmentUntil(view, threshold, Random(0))
        assertLTE(result.fragmentation(), threshold)

        val threshold2 = 20.0
        val result2 = defragmentUntil(result, threshold2, Random(0))
        assertLTE(result2.fragmentation(), threshold2)
    }

    @Test
    fun `defragmentUntil preserves existing invertedReveals`() {
        val initialReveals = listOf(
            setOf(1, 2, 3),
            setOf(5, 10),
            emptySet()
        )
        val view = RecipeView("recipeId",
            flaws = listOf(createFlaw(50.0)),
            invertedReveals = initialReveals
        )

        val threshold = 40.0
        val result = defragmentUntil(view, threshold, Random(0))
        assertLTE(result.fragmentation(), threshold)

        // Verify each step's reveals still contain the original reveals
        for (i in initialReveals.indices) {
            val initialSet = initialReveals[i]
            val resultSet = if (i < result.invertedReveals.size) result.invertedReveals[i] else emptySet()
            assertTrue(
                initialSet.all { it in resultSet },
                "Original reveals at step $i should be preserved: $initialSet not subset of $resultSet"
            )
        }

        // And result should have fewer inverted reveals overall (more characters revealed)
        val initialRevealsCount = view.invertedReveals.sumOf { it.size }
        val resultRevealsCount = result.invertedReveals.sumOf { it.size }
        assertTrue(
            resultRevealsCount <= initialRevealsCount,
            "Should not have more inverted reveals than initial: $resultRevealsCount <= $initialRevealsCount"
        )
    }

    @Test
    fun `defragmentUntil deterministic with same seed`() {
        val view1 = RecipeView("recipeId",
            flaws = listOf(createFlaw(55.0)),
            invertedReveals = emptyList()
        )
        val view2 = RecipeView("recipeId",
            flaws = listOf(createFlaw(55.0)),
            invertedReveals = emptyList()
        )

        val threshold = 40.0
        val seed = 12345L
        val result1 = defragmentUntil(view1, threshold, Random(seed))
        val result2 = defragmentUntil(view2, threshold, Random(seed))

        assertEquals(result1, result2)
    }

    private fun assertLTE(left: Double, right: Double) {
        if (left > right) {
            fail("expected $left <= $right")
        }
    }
}