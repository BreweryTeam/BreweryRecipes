package dev.jsinco.recipes.gui.integration

import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.inventory.ItemStack

object BreweryXGuiInterface : GuiIntegration {

    override fun createItem(recipeView: RecipeView): ItemStack? {
        val recipe = BRecipe.get(recipeView.recipeIdentifier) ?: return null
        val item = recipe.createBrew(10).createItem()
        val brewName = LegacyComponentSerializer.legacySection().deserialize(BUtil.color(recipe.getName(10)))
        val displayName = recipeView.translation(brewName)
        item?.setData(
            DataComponentTypes.CUSTOM_NAME, TranslationUtil.render(displayName)
                .colorIfAbsent(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        return item
    }
}
