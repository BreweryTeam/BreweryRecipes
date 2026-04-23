package dev.jsinco.recipes.integration

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.inventory.ItemStack

interface BrewingIntegration {
    fun createGuiItem(recipeDisplay: RecipeDisplay): GuiItem? {
        val item = createItem(recipeDisplay) ?: return null
        val displayName = recipeDisplay.displayName(item.effectiveName())
        val lore = recipeDisplay.toLore() ?: return null
        item.setData(
            DataComponentTypes.CUSTOM_NAME,
            GlobalTranslator.render(displayName, Recipes.recipesConfig.language)
        )
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore))
        return GuiItem(item, GuiItem.Type.NO_ACTION)
    }

    fun createItem(recipeDisplay: RecipeDisplay): ItemStack?
    fun brewDisplayName(identifier: String): Component?
    fun cookingMinuteTicks(): Long
    fun agingYearTicks(): Long
    fun allRecipes(): Collection<BreweryRecipe>
    fun getRecipe(id: String): BreweryRecipe?
    fun reload()

    fun enable(recipes: Recipes)
    fun score(recipe: BreweryRecipe): Double
}