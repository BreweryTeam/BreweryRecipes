package dev.jsinco.recipes.integration

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Color
import org.bukkit.inventory.ItemStack

interface BrewingIntegration {
    fun createGuiItem(recipeDisplay: RecipeDisplay): GuiItem? {
        val customItemConfig = BreweryRecipes.guiConfig.recipes.customItem
        val item = if (customItemConfig.enabled) {
            customItemConfig.item.generateItem()
        } else {
            createItem(recipeDisplay) ?: return null
        }
        val displayName = recipeDisplay.displayName(item.effectiveName())
        val lore = recipeDisplay.toLore() ?: return null
        item.setData(
            DataComponentTypes.CUSTOM_NAME,
            GlobalTranslator.render(displayName, BreweryRecipes.recipesConfig.language)
        )
        val loreConfig = BreweryRecipes.guiConfig.recipes.lore
        val finalLore = if (loreConfig.showBrewScore && recipeDisplay is BreweryRecipe) {
            val scoreComponent = scoreDisplayName(recipeDisplay)
            if (scoreComponent != null) {
                var line = TranslationUtil.render(
                    Component.translatable(
                        "breweryrecipes.gui.recipes.lore.quality",
                        Argument.component("qualitystars", scoreComponent)
                    ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .colorIfAbsent(NamedTextColor.GRAY)
                )
                if (loreConfig.applyIndentationToBrewScore) {
                    val prefix = if (loreConfig.indentation > 0) Component.text(" ".repeat(loreConfig.indentation)) else null
                    val suffix = if (loreConfig.trailingSpaces > 0) Component.text(" ".repeat(loreConfig.trailingSpaces)) else null
                    if (prefix != null) line = prefix.append(line)
                    if (suffix != null) line = line.append(suffix)
                }
                val scoreLines = mutableListOf<Component>()
                if (loreConfig.emptyLineAboveBrewScore) scoreLines.add(Component.empty())
                scoreLines.add(line)
                scoreLines + lore
            } else lore
        } else lore
        item.setData(DataComponentTypes.LORE, ItemLore.lore(finalLore))
        return GuiItem(item, GuiItem.Type.NO_ACTION)
    }

    fun createItem(recipeDisplay: RecipeDisplay): ItemStack?
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
}