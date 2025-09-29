package dev.jsinco.recipes.gui.integration

import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.api.brew.BrewingStep
import dev.jsinco.brewery.api.recipe.Recipe
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.gui.GuiItem
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.*

data class TBPRecipe(val recipe: Recipe<ItemStack>) : GuiItem {

    companion object {
        lateinit var tbpApi: TheBrewingProjectApi

        fun getApi(): TheBrewingProjectApi {
            if (!this::tbpApi.isInitialized) {
                this.tbpApi = Bukkit.getServicesManager().load(TheBrewingProjectApi::class.java)!!
            }
            return tbpApi
        }
    }


    override fun createItem(): ItemStack {
        val steps = recipe.steps
        val brew = getApi().brewManager.createBrew(steps)
        val score = brew.score(recipe)
        val item = recipe.getRecipeResult(BrewQuality.EXCELLENT).newBrewItem(score, brew, Brew.State.Seal(null))
        item.setData(
            DataComponentTypes.LORE, ItemLore.lore(
                steps.asSequence()
                    .map(this::renderStep)
                    .toList()
            )
        )
        return item
    }

    private fun renderStep(brewingStep: BrewingStep): Component {
        if (brewingStep is BrewingStep.Distill) {
            return Component.translatable(
                "recipes.display.recipe.step.distill", Argument.tagResolver(
                    Formatter.number("distill_runs", brewingStep.runs())
                )
            )
        }
        if (brewingStep is BrewingStep.Age) {
            return Component.translatable(
                "recipes.display.recipe.step.age", Argument.tagResolver(
                    Formatter.number("aging_years", brewingStep.time().moment()),
                    Placeholder.component(
                        "barrel_type",
                        Component.translatable("recipes.barrel.type." + brewingStep.barrelType().name.lowercase(Locale.ROOT))
                    )
                )
            )
        }
        if (brewingStep is BrewingStep.Mix) {
            return Component.translatable(
                "recipes.display.recipe.step.mix", Argument.tagResolver(
                    Placeholder.component(
                        "ingredients",
                        brewingStep.ingredients().entries.stream()
                            .map { entry ->
                                entry.key.displayName()
                                    .append { Component.text(entry.value).color(NamedTextColor.GOLD) }
                            }.collect(Component.toComponent())
                    ),
                    Formatter.number("mixing_time", brewingStep.time().moment())
                )
            )
        }
        if (brewingStep is BrewingStep.Cook) {
            return Component.translatable(
                "recipes.display.recipe.step.mix", Argument.tagResolver(
                    Placeholder.component(
                        "ingredients",
                        brewingStep.ingredients().entries.stream()
                            .map { entry ->
                                entry.key.displayName()
                                    .append { Component.text(entry.value).color(NamedTextColor.GOLD) }
                            }.collect(Component.toComponent())
                    ),
                    Formatter.number("cooking_time", brewingStep.time().moment()),
                    Placeholder.component(
                        "cauldron_type",
                        Component.translatable(
                            "recipes.cauldron.type." + brewingStep.cauldronType().name.lowercase(Locale.ROOT)
                        )
                    )
                )
            )
        }
        return Component.text { "Unknown component" }
    }
}
