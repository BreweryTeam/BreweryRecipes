package dev.jsinco.recipes.guis

import com.dre.brewery.BreweryPlugin
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.Util
import dev.jsinco.recipes.configuration.ConfigItemSection
import dev.jsinco.recipes.configuration.ConfigRecipeItem
import dev.jsinco.recipes.recipe.Recipe
import dev.jsinco.recipes.recipe.RecipeUtil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType

data class GuiItem(
    val material: Material,
    val slots: List<Int>,
    val name: String,
    val lore: List<String>,
    val glint: Boolean,
    val customModelData: Int
) {

    fun toItemStack(): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.setDisplayName(BUtil.color(name))
        meta.lore = Util.colorArrayList(lore)
        if (glint) {
            meta.addEnchant(Enchantment.MENDING, 1, true)
        }
        if (customModelData != 0) {
            meta.setCustomModelData(customModelData)
        }
        item.itemMeta = meta
        return item
    }

    companion object {
        private val plugin: BreweryPlugin = BreweryPlugin.getInstance()

        fun getAllGuiBorderItems(): List<Pair<List<Int>, ItemStack>> {
            val items = mutableListOf<Pair<List<Int>, ItemStack>>()
            val borderItems = Recipes.recipesConfig.gui.items.borderItems
            for (configItemSection in listOf(
                borderItems.border1,
                borderItems.border2,
                borderItems.border3,
                borderItems.border4,
                borderItems.border5
            )) {
                items.add(createGUIItem(getGUIItem(configItemSection), GuiItemType.BORDER_ITEM))
            }
            return items
        }

        fun getPageArrowItems(): Pair<Pair<List<Int>, ItemStack>, Pair<List<Int>, ItemStack>> {
            val left =
                createGUIItem(getGUIItem(Recipes.recipesConfig.gui.items.previousPage), GuiItemType.PREVIOUS_PAGE)
            val right = createGUIItem(getGUIItem(Recipes.recipesConfig.gui.items.nextPage), GuiItemType.NEXT_PAGE)
            return Pair(left, right)
        }

        fun getTotalRecipesItem(amount: Int, total: Int): Pair<List<Int>, ItemStack> {
            val itemPair =
                createGUIItem(getGUIItem(Recipes.recipesConfig.gui.items.totalRecipes), GuiItemType.BORDER_ITEM)
            val meta = itemPair.second.itemMeta!!
            meta.lore = meta.lore?.let {
                Util.colorArrayList(it.map { line ->
                    line.replace(
                        "%total_recipes%",
                        "$amount/$total"
                    )
                })
            }
            meta.setDisplayName(BUtil.color(meta.displayName.replace("%total_recipes%", "$amount/$total")))
            itemPair.second.itemMeta = meta
            return itemPair
        }

        fun createRecipeGuiItem(recipe: Recipe, known: Boolean): ItemStack {
            return if (known) {
                createRecipeGuiItem(recipe, Recipes.recipesConfig.gui.items.recipeGuiItem)
            } else {
                createRecipeGuiItem(recipe, Recipes.recipesConfig.gui.items.unknownRecipe)
            }
        }

        private fun createRecipeGuiItem(recipe: Recipe, configItemSection: ConfigRecipeItem): ItemStack {
            val item = ItemStack(configItemSection.material)
            val meta = item.itemMeta ?: return item

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
            if (item.type == Material.POTION) { // if it's a potion, set the color
                meta as PotionMeta
                meta.color = recipe.potionColor?.color
                meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
            meta.setDisplayName(
                BUtil.color( // display name
                    recipeItemStringHelper(configItemSection.name, recipe)
                )
            )
            if (configItemSection.glint) { // glint
                meta.addEnchant(Enchantment.MENDING, 1, true)
            }
            if (Recipes.recipesConfig.gui.items.recipeGuiItem.useRecipeCustomModelData) { // custom model data
                meta.setCustomModelData(recipe.customModelData)
            }


            // lore/ingredients
            val ingredients: MutableList<String> = mutableListOf()
            for (ingredient in recipe.ingredients) {
                ingredients.add(
                    BUtil.color(
                        configItemSection.ingredientFormat.replace("%amount%", ingredient.value.toString())
                            .replace(
                                "%ingredient%",
                                Util.itemNameFromMaterial(RecipeUtil.parseIngredientsName(ingredient.key))
                            )
                    )
                )
            }
            val lore: MutableList<String> = configItemSection.lore
                .map { BUtil.color(recipeItemStringHelper(it, recipe)) }.toMutableList()

            replaceWithList("%ingredients%", ingredients, lore)
            replaceWithList("%lore%", recipe.lore, lore)

            meta.lore = lore
            item.itemMeta = meta
            return item
        }

        private fun replaceWithList(key: String, replacements: List<String>, lore: MutableList<String>) {
            val placeHolderIndexes: List<Int> =
                lore.mapIndexedNotNull { index, line -> if (line.contains(key)) index else null }
            for (index in placeHolderIndexes) {
                lore.removeAt(index)
                lore.addAll(index, replacements)
            }
        }

        private fun recipeItemStringHelper(string: String, recipe: Recipe): String {
            return string
                .replace("%recipe%", RecipeUtil.parseRecipeName(recipe.name))
                .replace("%difficulty%", recipe.difficulty.toString())
                .replace("%cooking_time%", recipe.cookingTime.toString())
                .replace("%distill_runs%", recipe.distillRuns.toString())
                .replace("%age%", recipe.age.toString())
                .replace("%barrel_type%", Util.itemNameFromMaterial(recipe.woodType.name))
        }

        private fun createGUIItem(guiItem: GuiItem, guiItemType: GuiItemType): Pair<List<Int>, ItemStack> {
            val item = ItemStack(guiItem.material)
            val meta = item.itemMeta ?: return Pair(guiItem.slots, item)
            meta.setDisplayName(BUtil.color(guiItem.name))
            meta.lore = Util.colorArrayList(guiItem.lore)
            if (guiItem.glint) {
                meta.addEnchant(Enchantment.MENDING, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
            if (guiItem.customModelData != 0) {
                meta.setCustomModelData(guiItem.customModelData)
            }
            meta.persistentDataContainer.set(
                NamespacedKey(plugin, "gui-item-type"),
                PersistentDataType.STRING,
                guiItemType.name
            )

            item.itemMeta = meta
            return Pair(guiItem.slots, item)
        }

        private fun getGUIItem(configItemSection: ConfigItemSection): GuiItem {
            return GuiItem(
                configItemSection.material ?: Material.MAP,
                configItemSection.slots ?: listOf(),
                configItemSection.name ?: " ",
                configItemSection.lore ?: listOf(),
                configItemSection.glint ?: false,
                configItemSection.customModelData ?: 0
            )
        }
    }
}
