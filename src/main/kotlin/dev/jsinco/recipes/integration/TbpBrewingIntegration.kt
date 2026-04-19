package dev.jsinco.recipes.integration

import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.listeners.TheBrewingProjectListener
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.util.TBPRecipeConverter
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
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
        val result = recipe.getRecipeResult(BrewQuality.EXCELLENT)
        val brew = getApi().brewManager.createBrew(recipe.steps)
        val item = result.newBrewItem(brew.score(recipe), brew, Brew.State.Brewing())
        return item
    }

    override fun brewDisplayName(identifier: String): Component? {
        val recipe = getApi().recipeRegistry.getRecipe(identifier).getOrNull() ?: return null
        return recipe.getRecipeResult(BrewQuality.EXCELLENT).displayName()
    }

    override fun recipeResult(recipe: BreweryRecipe): BrewingIntegration.RecipeResult {
        val steps = TBPRecipeConverter.convert(recipe)
        val brew = getApi().brewManager.createBrew(steps)
        val item = getApi().brewManager.toItem(brew, Brew.State.Other())
        val score = brew.closestRecipe(getApi().recipeRegistry).orElse(null)
            ?.let(brew::score)
        val failed = score
            ?.let { score -> score.brewQuality() == null }
            ?: true
        return BrewingIntegration.RecipeResult(
            item.effectiveName(),
            failed,
            score?.score() ?: 0.0
        )
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

    override fun enable(recipes: Recipes) {
        Bukkit.getPluginManager().registerEvents(TheBrewingProjectListener(getApi()), recipes)
    }
}