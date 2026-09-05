package dev.jsinco.recipes.integration

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.UndiscoveredRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Color
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

interface BrewingIntegration {
    fun createGuiItem(recipeDisplay: RecipeDisplay): GuiItem? {
        val item = if (recipeDisplay is UndiscoveredRecipe) {
            BreweryRecipes.guiConfig.recipes.undiscoveredItem.generateItem()
        } else {
            val customItemConfig = BreweryRecipes.guiConfig.recipes.customItem
            if (customItemConfig.enabled) {
                customItemConfig.item.generateItem()
            } else {
                createItem(recipeDisplay.recipeKey()) ?: return null
            }
        }
        val brewDisplayName = brewDisplayName(recipeDisplay.recipeKey())
            ?.color(null)
            ?.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            ?: return null
        val displayName = recipeDisplay.displayName(brewDisplayName)
        val lore = recipeDisplay.toLore() ?: return null
        item.setData(
            DataComponentTypes.CUSTOM_NAME,
            GlobalTranslator.render(displayName, BreweryRecipes.recipesConfig.language)
        )
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore))
        return GuiItem(item, GuiItem.Type.NO_ACTION)
    }

    fun createItem(identifier: String): ItemStack?
    fun brewDisplayName(identifier: String): Component?
    fun brewIngredientColor(ingredientKey: String): Color?
    fun cookingMinuteTicks(): Long
    fun agingYearTicks(): Long
    fun allRecipes(): Collection<BreweryRecipe>
    fun getRecipe(id: String): BreweryRecipe?
    fun reload()

    fun enable(breweryRecipes: BreweryRecipes)
    fun score(recipe: BreweryRecipe): Double
    fun scoreDisplayName(recipe: BreweryRecipe): Component? = null

    /**
     * The player's drunkenness, from 0.0 to 100.0
     */
    fun drunkenness(player: OfflinePlayer): Double
}