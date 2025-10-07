package dev.jsinco.recipes.core

import com.google.common.collect.ImmutableList
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.type.FlawTypeCollection
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
import java.util.*
import kotlin.random.Random

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
                    .map { GlobalTranslator.render(it, Locale.ENGLISH) }
            )
        )
        return itemStack
    }

    fun generateCompletedView(): RecipeView {
        return RecipeView(this.identifier, listOf())
    }

    fun generate(expectedFlawLevel: Double): RecipeView {
        val collection = FlawTypeCollection.entries.toTypedArray().random()
        return generate(expectedFlawLevel, collection)
    }

    fun generate(expectedFlawLevel: Double, collection: FlawTypeCollection): RecipeView {
        val flaws = mutableListOf<Flaw>()
        var flawLevel = 0.0
        val remainingFlaws = collection.flawTypes.toMutableList()
        while (expectedFlawLevel > flawLevel && !remainingFlaws.isEmpty()) {
            val targetIntensity = Random.nextDouble(expectedFlawLevel.coerceIn(0.0, 90.0), 100.0)
            val type = collection.flawTypes.random()
            remainingFlaws.remove(type)

            val extent = collection.compileExtent(type, steps.size)
            val config = collection.compileConfig(type, extent, targetIntensity)

            flawLevel += extent.obscurationLevel(steps.size) * type.estimatedObscurationIntensity(targetIntensity)
            flaws.add(Flaw(type, config))
        }
        return RecipeView(this.identifier, listOf(FlawBundle(flaws)))
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