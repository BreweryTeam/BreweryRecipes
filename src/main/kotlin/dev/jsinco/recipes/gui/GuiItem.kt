package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

data class GuiItem(private val item: ItemStack, val type: Type) {
    companion object {
        private val TYPE_KEY = Recipes.key("gui_item_type")
    }

    fun item(): ItemStack {
        val output = item.clone()
        output.editPersistentDataContainer { persistentDataContainer ->
            persistentDataContainer.set(TYPE_KEY, PersistentDataType.STRING, type.identifier())
        }
        return output
    }

    enum class Type {
        NEXT_PAGE,
        PREVIOUS_PAGE,
        NO_ACTION;

        fun identifier() = name.lowercase(Locale.ROOT)
    }
}
