package dev.jsinco.recipes.gui.integration

import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.RecipeWriter
import dev.jsinco.recipes.gui.RecipeItem
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

data class TBPRecipe(val recipeView: RecipeView) : RecipeItem {

    companion object {
        lateinit var tbpApi: TheBrewingProjectApi

        fun getApi(): TheBrewingProjectApi {
            if (!this::tbpApi.isInitialized) {
                this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
            }
            return tbpApi
        }
    }

    override fun createItem(): ItemStack? {
        return RecipeWriter.writeToItem(recipeView)
    }
}
