package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

data class GuiItem(private val item: ItemStack, val type: Type) {
    companion object {
        private val TYPE_KEY = BreweryRecipes.key("gui_item_type")
    }

    init {
        item.editPersistentDataContainer { persistentDataContainer ->
            persistentDataContainer.set(TYPE_KEY, PersistentDataType.STRING, type.identifier())
        }
    }

    fun item(): ItemStack = item.clone()

    enum class Type {
        NEXT_PAGE,
        PREVIOUS_PAGE,
        NO_ACTION,
        SWITCH_MODE,
        SET_MODE_FRAGMENTS,
        SET_MODE_BREWED;

        fun identifier() = name.lowercase(Locale.ROOT)

        fun targetMode(): RecipeBookMode? = when (this) {
            SET_MODE_FRAGMENTS -> RecipeBookMode.FRAGMENTS
            SET_MODE_BREWED -> RecipeBookMode.BREWED
            else -> null
        }
    }
}
