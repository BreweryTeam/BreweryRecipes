package dev.jsinco.recipes.integration

import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.util.TBPRecipeConverter
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

object TbpBrewingIntegration : BrewingIntegration {

    lateinit var tbpApi: TheBrewingProjectApi
    private lateinit var recipeMap: Map<String, BreweryRecipe>

    fun getApi(): TheBrewingProjectApi {
        if (!this::tbpApi.isInitialized) {
            this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
        }
        return tbpApi
    }

    override fun createItem(recipeView: RecipeView): ItemStack? {
        val recipe = getApi().recipeRegistry.getRecipe(recipeView.recipeIdentifier).getOrNull() ?: return null
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
        val failed = brew.closestRecipe(getApi().recipeRegistry).orElse(null)
            ?.let(brew::score)
            ?.let { score -> score.brewQuality() == null }
            ?: true
        return BrewingIntegration.RecipeResult(
            item.effectiveName(),
            failed
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
        } catch (e: ClassNotFoundException) {
            return 20 * 60 * 20 // default
        }
    }

    override fun getRecipe(id: String): BreweryRecipe? {
        if (this::recipeMap.isInitialized && !recipeMap.isEmpty()) {
            return recipeMap[id]
        }
        if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi::class.java)) {
            return null
        }
        val provider = getApi()
        recipeMap = provider.recipeRegistry.recipes
            .map { TBPRecipeConverter.convert(it) }
            .associateBy { it.identifier }
        return recipeMap[id]
    }

    override fun reload() {
        recipeMap = getApi().recipeRegistry.recipes
            .map { TBPRecipeConverter.convert(it) }
            .associateBy { it.identifier }
    }

    override fun enable(recipes: Recipes) {
        Bukkit.getPluginManager().registerEvents(TheBrewingProjectListener(getApi()), recipes)
    }
}