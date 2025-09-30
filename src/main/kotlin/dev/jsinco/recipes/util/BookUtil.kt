package dev.jsinco.recipes.util

import dev.jsinco.recipes.Recipes
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object BookUtil {
    private val BOOK_KEY: NamespacedKey = Recipes.key("recipe-book")!!

    fun createBook(): ItemStack {
        val item = ItemStack(Material.WRITTEN_BOOK)
        item.unsetData(DataComponentTypes.WRITTEN_BOOK_CONTENT)
        item.setData(
            DataComponentTypes.CUSTOM_NAME, Component.text("Recipes")
                .colorIfAbsent(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        item.editPersistentDataContainer { pdc ->
            pdc.set(BOOK_KEY, PersistentDataType.INTEGER, 1)
        }
        return item
    }

    fun isBook(item: ItemStack): Boolean {
        return item.persistentDataContainer.has(BOOK_KEY, PersistentDataType.INTEGER)
    }
}