package dev.jsinco.recipes.integration

import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.listeners.TheBrewingProjectListener
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.util.TBPRecipeConverter
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

object TbpBrewingIntegration : BrewingIntegration {

    private lateinit var tbpApi: TheBrewingProjectApi
    private lateinit var recipeMap: Map<String, BreweryRecipe>

    fun getApi(): TheBrewingProjectApi {
        if (!this::tbpApi.isInitialized) {
            this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
        }
        return tbpApi
    }

    override fun createItem(recipeDisplay: RecipeDisplay): ItemStack? {
        val recipe = getApi().recipeRegistry.getRecipe(recipeDisplay.recipeKey()).getOrNull() ?: return null
        return recipe.getRecipeResult(BrewQuality.EXCELLENT).newLorelessItem()
    }

    override fun brewDisplayName(identifier: String): Component? {
        val recipe = getApi().recipeRegistry.getRecipe(identifier).getOrNull() ?: return null
        return recipe.getRecipeResult(BrewQuality.EXCELLENT).displayName()
    }

    override fun cookingMinuteTicks(): Long {
        try {
            Class.forName("dev.jsinco.brewery.api.config.Configuration")
            return getApi().configuration.cauldrons().cookingMinuteTicks()
        } catch (e: ClassNotFoundException) {
            return 20 * 60 // default
        }
    }

    override fun agingYearTicks(): Long {
        try {
            Class.forName("dev.jsinco.brewery.api.config.Configuration")
            return getApi().configuration.barrels().agingYearTicks()
        } catch (_: ClassNotFoundException) {
            return 20 * 60 * 20 // default
        }
    }

    override fun allRecipes(): Collection<BreweryRecipe> {
        return getRecipeMap().values
    }

    override fun getRecipe(id: String): BreweryRecipe? {
        return getRecipeMap()[id]
    }

    override fun reload() {
        recipeMap = getApi().recipeRegistry.recipes
            .map { TBPRecipeConverter.convert(it) }
            .associateBy { it.identifier }
    }

    private fun getRecipeMap(): Map<String, BreweryRecipe> {
        if (this::recipeMap.isInitialized && !recipeMap.isEmpty()) {
            return recipeMap
        }
        if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi::class.java)) {
            return mapOf()
        }
        val provider = getApi()
        recipeMap = provider.recipeRegistry.recipes
            .map { TBPRecipeConverter.convert(it) }
            .associateBy { it.identifier }
        return recipeMap
    }

    override fun brewIngredientColor(ingredientKey: String): Color? {
        val normalizedKey = ingredientKey.substringAfterLast(':')
        return (getApi().recipeRegistry.getRecipe(normalizedKey).getOrNull()
            ?: getApi().recipeRegistry.getRecipe(ingredientKey).getOrNull())
            ?.getRecipeResult(BrewQuality.EXCELLENT)
            ?.newLorelessItem()
            ?.getData(DataComponentTypes.POTION_CONTENTS)
            ?.customColor()
    }

    override fun enable(breweryRecipes: BreweryRecipes) {
        Bukkit.getPluginManager().registerEvents(TheBrewingProjectListener(getApi()), breweryRecipes)
    }

    override fun score(recipe: BreweryRecipe): Double {
        val steps = TBPRecipeConverter.convert(recipe)
        val brew = getApi().brewManager.createBrew(steps)
        val tbpRecipe = getApi().recipeRegistry.getRecipe(recipe.identifier).orElse(null) ?: return 0.0
        return brew.score(tbpRecipe).score()
    }

    override fun scoreDisplayName(recipe: BreweryRecipe): Component? {
        val steps = TBPRecipeConverter.convert(recipe)
        val brew = getApi().brewManager.createBrew(steps)
        val tbpRecipe = getApi().recipeRegistry.getRecipe(recipe.identifier).orElse(null) ?: return null
        return brew.score(tbpRecipe).displayName()
    }
}