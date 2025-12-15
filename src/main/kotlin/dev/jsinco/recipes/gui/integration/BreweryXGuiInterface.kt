package dev.jsinco.recipes.gui.integration

import com.dre.brewery.configuration.ConfigManager
import com.dre.brewery.configuration.files.Config
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.recipe.RecipeView
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
}
