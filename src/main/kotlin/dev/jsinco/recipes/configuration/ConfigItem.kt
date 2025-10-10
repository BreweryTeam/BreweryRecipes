package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.TooltipDisplay.tooltipDisplay
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack


class ConfigItem : OkaeriConfig() {

    var material: Material? = null
    var name: Component? = null
    var lore: Lore? = null
    var glint: Boolean = false
    var customModelData: Int? = null
    var itemModel: Key? = null
    var noText = false

    fun generateItem(): ItemStack {
        val item = ItemStack(material ?: Material.RED_STAINED_GLASS_PANE)
        name?.let {
            item.setData(
                DataComponentTypes.CUSTOM_NAME, it
                    .decorationIfAbsent(
                        TextDecoration.ITALIC,
                        TextDecoration.State.FALSE
                    ).colorIfAbsent(NamedTextColor.WHITE)
            )
        }
        lore?.components?.let {
            item.setData(
                DataComponentTypes.LORE, ItemLore.lore(
                    it.map { component ->
                        component.decorationIfAbsent(
                            TextDecoration.ITALIC,
                            TextDecoration.State.FALSE
                        ).colorIfAbsent(NamedTextColor.WHITE)
                    }
                ))
        }
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint)
        customModelData?.let {
            item.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                    .addFloat(it.toFloat())
            )
        }
        itemModel?.let {
            item.setData(DataComponentTypes.ITEM_MODEL, it)
        }
        if (noText) {
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay().hideTooltip(true))
        }
        return item
    }


    class Builder {
        private val configItemSection = ConfigItem()

        fun material(material: Material) = apply { configItemSection.material = material }
        fun name(name: String) = apply { configItemSection.name = MiniMessage.miniMessage().deserialize(name) }
        fun name(name: Component) = apply { configItemSection.name = name }
        fun lore(vararg lore: String) = apply {
            configItemSection.lore = Lore(
                *lore
                    .map { MiniMessage.miniMessage().deserialize(it) }
                    .toList()
                    .toTypedArray()
            )
        }

        fun lore(lore: List<Component>) = apply { configItemSection.lore = Lore(*lore.toTypedArray()) }

        fun glint(glint: Boolean) = apply { configItemSection.glint = glint }
        fun customModelData(customModelData: Int) = apply { configItemSection.customModelData = customModelData }
        fun itemModel(itemModel: String) = apply { configItemSection.itemModel = NamespacedKey.fromString(itemModel) }
        fun itemModel(itemModel: Key) = apply { configItemSection.itemModel = itemModel }
        fun noText(noText: Boolean) = apply { configItemSection.noText = noText }

        fun build() = configItemSection
    }
}