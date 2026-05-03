package dev.jsinco.recipes.util

import dev.jsinco.recipes.Recipes
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object BookUtil {

    fun createBook(): ItemStack {
        val item = Recipes.recipesConfig.book.item.generateItem()
        item.editPersistentDataContainer { pdc ->
            pdc.set(PdcKeys.BOOK_KEY, PersistentDataType.INTEGER, 1)
        }
        return item
    }

    fun isBook(item: ItemStack): Boolean {
        return item.persistentDataContainer.has(PdcKeys.BOOK_KEY, PersistentDataType.INTEGER)
    }
}