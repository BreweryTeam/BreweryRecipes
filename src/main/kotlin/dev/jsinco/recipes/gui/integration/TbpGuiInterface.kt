package dev.jsinco.recipes.gui.integration

import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.RecipeWriter
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

object TbpGuiInterface : GuiIntegration {

    lateinit var tbpApi: TheBrewingProjectApi

    fun getApi(): TheBrewingProjectApi {
        if (!this::tbpApi.isInitialized) {
            this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
        }
        return tbpApi
    }

    override fun createItem(recipeView: RecipeView): ItemStack? {
        val recipe = getApi().recipeRegistry.getRecipe(recipeView.recipeIdentifier).getOrNull() ?: return null
        val result = recipe.getRecipeResult(BrewQuality.EXCELLENT) as BukkitRecipeResult
        val brew = getApi().brewManager.createBrew(recipe.steps)
        val item = result.newBrewItem(brew.score(recipe), brew, Brew.State.Brewing())
        val brewDisplayName = (recipe.getRecipeResult(BrewQuality.EXCELLENT) as BukkitRecipeResult).name
        val displayName = recipeView.translation(MiniMessage.miniMessage().deserialize(brewDisplayName))
        item?.setData(
            DataComponentTypes.CUSTOM_NAME, TranslationUtil.render(displayName)
                .colorIfAbsent(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        return item
    }
}
