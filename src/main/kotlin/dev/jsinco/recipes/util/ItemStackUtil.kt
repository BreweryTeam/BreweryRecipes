package dev.jsinco.recipes.util

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

// may be removed
object ItemStackUtil {


    fun editMeta(itemStack: ItemStack, block: (meta: ItemMeta) -> Unit): ItemStack {
        val meta = itemStack.itemMeta
        block(meta)
        itemStack.itemMeta = meta
        return itemStack
    }

    fun <P, C : Any> setPersistentData(itemStack: ItemStack, key: NamespacedKey, type: PersistentDataType<P, C>, value: C): ItemStack {
        return editMeta(itemStack) {
            it.persistentDataContainer.set(key, type, value)
        }
    }

    fun hasPersistentKey(itemStack: ItemStack, key: NamespacedKey): Boolean = itemStack.itemMeta.persistentDataContainer.has(key,)

}