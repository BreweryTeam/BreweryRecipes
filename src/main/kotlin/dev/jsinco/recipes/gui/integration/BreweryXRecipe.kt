package dev.jsinco.recipes.gui.integration

import com.dre.brewery.recipe.BRecipe
import dev.jsinco.recipes.gui.GuiItem
import org.bukkit.inventory.ItemStack

data class BreweryXRecipe(val backing: BRecipe) : GuiItem {


    override fun createItem(): ItemStack {
        TODO("Not yet implemented")
    }
}
