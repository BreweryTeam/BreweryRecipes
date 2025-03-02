package dev.jsinco.recipes

import com.dre.brewery.BreweryPlugin
import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.configuration.RecipesConfig
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType

object Util {
    val plugin: BreweryPlugin = BreweryPlugin.getInstance()
    private val config: RecipesConfig = Recipes.configManager.getConfig(RecipesConfig::class.java)


    @JvmStatic
    fun colorArrayList(list: List<String>): List<String> {
        return list.map { BUtil.color(it) }
    }

    @JvmStatic
    fun giveItem(player: Player, item: ItemStack) {
        for (i in 0..35) {
            if (player.inventory.getItem(i) == null || player.inventory.getItem(i)!!.isSimilar(item)) {
                player.inventory.addItem(item)
                break
            } else if (i == 35) {
                player.world.dropItem(player.location, item)
            }
        }
    }

    @JvmStatic
    fun itemNameFromMaterial(item: String): String {
        var name = item.lowercase().replace("_", " ")
        name = name.substring(0, 1).uppercase() + name.substring(1)
        for (i in name.indices) {
            if (name[i] == ' ') {
                name = name.substring(0, i) + " " + name[i + 1].toString().uppercase() + name.substring(i + 2)
            }
        }
        return name
    }


    @JvmStatic
    fun getRecipeBookItem(): ItemStack {
        val item = ItemStack(config.recipeBookItem.material ?: Material.BOOK)
        val meta = item.itemMeta ?: return item
        meta.setDisplayName(BUtil.color(config.recipeBookItem.name))
        meta.lore = config.recipeBookItem.lore?.map { BUtil.color(it) }
        if (config.recipeBookItem.glint == true) {
            meta.addEnchant(Enchantment.MENDING, 1, true)
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        meta.persistentDataContainer.set(NamespacedKey(plugin, "recipe-book"), PersistentDataType.INTEGER, 0)
        item.itemMeta = meta
        return item
    }

    fun hasRecipePermission(player: Player, recipeKey: String): Boolean {
        val permissionNode = config.recipePermissionNode.replace("%recipe%", recipeKey)
        // PermissionDefault.FALSE is required to prevent OPs from unlocking all recipes
        return player.hasPermission(Permission(permissionNode, PermissionDefault.FALSE))
    }
}