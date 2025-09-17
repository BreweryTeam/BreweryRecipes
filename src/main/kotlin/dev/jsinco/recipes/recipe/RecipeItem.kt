package dev.jsinco.recipes.recipe

import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.Util
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class RecipeItem(recipe: Recipe) {

    val item = ItemStack(Recipes.recipesConfig.recipeItem.material ?: Material.PAPER)

    init {
        val meta = item.itemMeta!!

        meta.setDisplayName(
            BUtil.color(
                Recipes.recipesConfig.recipeItem.name?.replace("%recipe%", RecipeUtil.parseRecipeName(recipe.name))
                    ?: "&#F7FFC9${RecipeUtil.parseRecipeName(recipe.name)} &fRecipe"
            )
        )
        meta.lore = Util.colorArrayList(Recipes.recipesConfig.recipeItem.lore?.map {
            it.replace(
                "%recipe%",
                RecipeUtil.parseRecipeName(recipe.name)
            )
        } ?: listOf())
        meta.persistentDataContainer.set(
            NamespacedKey(Recipes.instance, "recipe-key"),
            PersistentDataType.STRING,
            recipe.recipeKey
        )
        if (Recipes.recipesConfig.recipeItem.glint == true) {
            meta.addEnchant(Enchantment.MENDING, 1, true)
        }
        if (Recipes.recipesConfig.recipeItem.customModelData != null) {
            meta.setCustomModelData(Recipes.recipesConfig.recipeItem.customModelData)
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        item.itemMeta = meta
    }


}