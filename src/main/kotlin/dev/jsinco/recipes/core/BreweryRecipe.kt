package dev.jsinco.recipes.core

import com.google.common.collect.ImmutableList
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.core.process.Ingredient
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.steps.AgeStep
import dev.jsinco.recipes.core.process.steps.CookStep
import dev.jsinco.recipes.core.process.steps.DistillStep
import dev.jsinco.recipes.core.process.steps.MixStep
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class BreweryRecipe(val identifier: String, val steps: List<Step>) {

    fun lootItem(): ItemStack {
        val itemStack = ItemStack(Material.PAPER)
        itemStack.setData(
            DataComponentTypes.CUSTOM_NAME,
            TranslationUtil.render(
                Component.translatable("recipes.loot.new.brew.recipe")
                    .colorIfAbsent(NamedTextColor.WHITE)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        )
        itemStack.setData(
            DataComponentTypes.LORE, ItemLore.lore(
                listOf(Component.translatable("recipes.loot.right.click.to.discover"))
                    .map { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) }
                    .map { it.colorIfAbsent(NamedTextColor.GRAY) }
                    .map(TranslationUtil::render)
            )
        )
        return itemStack
    }

    fun generateCompletedView(): RecipeView {
        return RecipeView(this.identifier, listOf())
    }

    fun generate(expectedFlawLevel: Double): RecipeView {
        val collection = RecipeViewCreator.Type.entries.random()
        return generate(expectedFlawLevel, collection)
    }

    fun generate(expectedFlawLevel: Double, flawViewType: RecipeViewCreator.Type): RecipeView {
        return flawViewType.recipeViewCreator.create(this, expectedFlawLevel)
    }

    class Builder(private val identifier: String) {
        private val stepsBuilder = ImmutableList.Builder<Step>()

        fun mix(ticks: Long, cauldronType: String, ingredients: Map<Ingredient, Int>) = apply {
            stepsBuilder.add(MixStep(ticks, MixStep.CauldronType.fromString(cauldronType), ingredients))
        }

        fun cook(ticks: Long, cauldronType: String, ingredients: Map<Ingredient, Int>) = apply {
            stepsBuilder.add(CookStep(ticks, CookStep.CauldronType.fromString(cauldronType), ingredients))
        }

        fun distill(count: Long) = apply { stepsBuilder.add(DistillStep(count)) }

        fun age(ticks: Long, barrelType: String) = apply {
            stepsBuilder.add(AgeStep(ticks, AgeStep.BarrelType.fromString(barrelType)))
        }

        fun build() = BreweryRecipe(identifier, stepsBuilder.build())
    }

}