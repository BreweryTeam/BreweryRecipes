package dev.jsinco.recipes.gui.integration

import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.RecipeWriter
import dev.jsinco.recipes.gui.RecipeItem
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

data class TBPRecipe(val recipeView: RecipeView) : RecipeItem {

    companion object {
        lateinit var tbpApi: TheBrewingProjectApi

        fun getApi(): TheBrewingProjectApi {
            if (!this::tbpApi.isInitialized) {
                this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
            }
            return tbpApi
        }
    }

    override fun createItem(): ItemStack? {
        val tbp = getApi()
        val recipe = tbp.recipeRegistry.getRecipe(recipeView.recipeIdentifier).getOrNull() ?: return null
        val brewDisplayName = (recipe.getRecipeResult(BrewQuality.EXCELLENT) as BukkitRecipeResult).name
        val displayName = recipeView.translation(MiniMessage.miniMessage().deserialize(brewDisplayName))
        val item = RecipeWriter.writeToItem(recipeView)
        item?.setData(
            DataComponentTypes.CUSTOM_NAME, TranslationUtil.render(displayName)
                .colorIfAbsent(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        return item;
    }
}
