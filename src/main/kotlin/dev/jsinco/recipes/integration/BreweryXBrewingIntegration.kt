package dev.jsinco.recipes.integration

import com.dre.brewery.configuration.ConfigManager
import com.dre.brewery.configuration.files.Config
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.util.BreweryXRecipeConverter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Color
import org.bukkit.inventory.ItemStack

object BreweryXBrewingIntegration : BrewingIntegration {


    private lateinit var recipeMap: Map<String, BreweryRecipe>

    override fun createItem(recipeDisplay: RecipeDisplay): ItemStack? {
        val recipe = BRecipe.getRecipes().first { it.id.equals(recipeDisplay.recipeKey(), true) } ?: return null
        return recipe.createBrew(10).createItem()
    }

    override fun brewDisplayName(identifier: String): Component? {
        val recipe = BRecipe.getRecipes().first { it.id.equals(identifier, true) } ?: return null
        return LegacyComponentSerializer.legacySection().deserialize(BUtil.color(recipe.getName(10)))
    }

    override fun cookingMinuteTicks(): Long {
        return 20 * 60 // not configurable
    }

    override fun agingYearTicks(): Long {
        try {
            Class.forName("com.dre.brewery.configuration.ConfigManager")
            return (ConfigManager.getConfig(Config::class.java).agingYearDuration * 60 * 20).toLong();
        } catch (e: ClassNotFoundException) {
            return 20 * 60 * 20 // default
        }
    }

    override fun allRecipes(): Collection<BreweryRecipe> {
        return getRecipeMap().values
    }

    override fun getRecipe(id: String): BreweryRecipe? {
        return getRecipeMap()[id]
    }

    private fun getRecipeMap(): Map<String, BreweryRecipe> {
        if (this::recipeMap.isInitialized && !recipeMap.isEmpty()) {
            return recipeMap
        }
        recipeMap = BRecipe.getRecipes()
            .map { BreweryXRecipeConverter.convert(it) }
            .associateBy { it.identifier }
        return recipeMap
    }

    override fun reload() {
        recipeMap = BRecipe.getRecipes()
            .map { BreweryXRecipeConverter.convert(it) }
            .associateBy { it.identifier }
    }

    override fun enable(recipes: Recipes) {
    }

    override fun brewIngredientColor(ingredientKey: String): Color? =
        BRecipe.getRecipes().firstOrNull { it.id.equals(ingredientKey, ignoreCase = true) }?.color?.color

    override fun score(recipe: BreweryRecipe): Double = 1.0
}