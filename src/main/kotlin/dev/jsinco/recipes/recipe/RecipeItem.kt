package dev.jsinco.recipes.recipe

import com.dre.brewery.BreweryPlugin
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.Util
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class RecipeItem (recipe: Recipe) {

    companion object {
        private val plugin: BreweryPlugin = BreweryPlugin.getInstance()
    }

    private val config: RecipesConfig = Recipes.configManager.getConfig(RecipesConfig::class.java)

    val item = ItemStack(config.recipeItem.material ?: Material.PAPER)

    init {
        val meta = item.itemMeta!!

        meta.setDisplayName(BUtil.color(config.recipeItem.name?.replace("%recipe%", RecipeUtil.parseRecipeName(recipe.name))
            ?: "&#F7FFC9${RecipeUtil.parseRecipeName(recipe.name)} &fRecipe"))
        meta.lore = Util.colorArrayList(config.recipeItem.lore?.map { it.replace("%recipe%", RecipeUtil.parseRecipeName(recipe.name)) } ?: listOf())
        meta.persistentDataContainer.set(NamespacedKey(plugin, "recipe-key"), PersistentDataType.STRING, recipe.recipeKey)
        if (config.recipeItem.glint == true) {
            meta.addEnchant(Enchantment.MENDING, 1, true)
        }
        if (config.recipeItem.customModelData != null) {
            meta.setCustomModelData(config.recipeItem.customModelData)
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        item.itemMeta = meta
    }



}