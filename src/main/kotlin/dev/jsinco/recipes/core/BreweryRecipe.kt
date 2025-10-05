package dev.jsinco.recipes.core

import com.google.common.collect.ImmutableList
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
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

    // TODO: This doesn't feel sophisticated enough yet
    fun generate(expectedFlawLevel: Double): RecipeView {
        val flaws = mutableListOf<Flaw>()
        var flawLevel = 0.0
        val collection = FlawTypeCollection.entries.toTypedArray().random()

        while (expectedFlawLevel > flawLevel) {
            val intensity = Random.nextDouble(10.0, (expectedFlawLevel - flawLevel).coerceIn(20.0, 100.0))
            val seed = Random.nextInt()
            val type = collection.flawTypes.random()

            val extent = when (Random.nextInt(3)) {
                0 -> FlawExtent.Everywhere()
                1 -> FlawExtent.WholeStep(Random.nextInt(steps.size))
                2 -> {
                    val start = Random.nextInt(25)
                    val stop = Random.nextInt(start + 10, start + 25)
                    FlawExtent.PartialStep(Random.nextInt(steps.size), start, stop)
                }

                else -> FlawExtent.Everywhere()
            }

            val config = FlawConfig(extent, seed, intensity)

            flawLevel += extent.obscurationLevel(steps.size) * type.estimatedObscurationIntensity(intensity)
            flaws.add(Flaw(type, config))
        }
        return RecipeView(this.identifier, listOf(FlawBundle(flaws)))
    }

    // TODO: Make the BX and TBP integrations use this builder to construct all of their registered recipes, so we can make recipes for them
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