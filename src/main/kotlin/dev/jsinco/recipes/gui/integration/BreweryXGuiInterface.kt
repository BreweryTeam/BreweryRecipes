package dev.jsinco.recipes.gui.integration

import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.core.RecipeView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.inventory.ItemStack

object BreweryXGuiInterface : GuiIntegration {

    override fun createItem(recipeView: RecipeView): ItemStack? {
        val recipe = BRecipe.getRecipes().first { it.id.equals(recipeView.recipeIdentifier, true) } ?: return null
        return recipe.createBrew(10).createItem()
    }

    override fun brewDisplayName(identifier: String): Component? {
        val recipe = BRecipe.getRecipes().first { it.id.equals(identifier, true) } ?: return null
        return LegacyComponentSerializer.legacySection().deserialize(BUtil.color(recipe.getName(10)))
    }
}
