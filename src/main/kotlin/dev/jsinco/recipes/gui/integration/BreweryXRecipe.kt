package dev.jsinco.recipes.gui.integration

import com.dre.brewery.recipe.BRecipe
import dev.jsinco.recipes.gui.RecipeItem
import org.bukkit.inventory.ItemStack

data class BreweryXRecipe(val backing: BRecipe) : RecipeItem {


    override fun createItem(): ItemStack {
        TODO("Not yet implemented")
    }

    override fun key(): String {
        return backing.recipeName
    }

    override fun loot(): ItemStack {
        TODO("Not yet implemented")
    }
}
