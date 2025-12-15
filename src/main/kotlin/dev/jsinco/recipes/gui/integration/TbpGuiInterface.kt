package dev.jsinco.recipes.gui.integration

import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult
import dev.jsinco.recipes.recipe.RecipeView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

object TbpGuiInterface : GuiIntegration {

    lateinit var tbpApi: TheBrewingProjectApi

    fun getApi(): TheBrewingProjectApi {
        if (!this::tbpApi.isInitialized) {
            this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
        }
        return tbpApi
    }

    override fun createItem(recipeView: RecipeView): ItemStack? {
        val recipe = getApi().recipeRegistry.getRecipe(recipeView.recipeIdentifier).getOrNull() ?: return null
        val result = recipe.getRecipeResult(BrewQuality.EXCELLENT) as BukkitRecipeResult
        val brew = getApi().brewManager.createBrew(recipe.steps)
        val item = result.newBrewItem(brew.score(recipe), brew, Brew.State.Brewing())
        return item
    }

    override fun brewDisplayName(identifier: String): Component? {
        val recipe = getApi().recipeRegistry.getRecipe(identifier).getOrNull() ?: return null
        return MiniMessage.miniMessage()
            .deserialize((recipe.getRecipeResult(BrewQuality.EXCELLENT) as BukkitRecipeResult).name)
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
}
