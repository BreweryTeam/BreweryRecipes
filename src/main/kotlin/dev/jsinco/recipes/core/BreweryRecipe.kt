package dev.jsinco.recipes.core

import com.google.common.collect.ImmutableList
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.number.InaccuracyFlawType
import dev.jsinco.recipes.core.flaws.text.AmnesiaFlawType
import dev.jsinco.recipes.core.flaws.text.ObfuscationFlawType
import dev.jsinco.recipes.core.flaws.text.OmissionFlawType
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

data class BreweryRecipe(val identifier: String, val steps: List<Step>) {

    companion object {
        val RANDOM = Random()
    }

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

    fun generate(flawLevel: Double): RecipeView {
        val seeds = listOf(RANDOM.nextInt())
        val type = when (RANDOM.nextInt(4)) {
            0 -> ObfuscationFlawType(flawLevel, seeds)
            1 -> AmnesiaFlawType(flawLevel, seeds)
            2 -> OmissionFlawType(flawLevel, seeds)
            3 -> InaccuracyFlawType(flawLevel, seeds)
            else -> ObfuscationFlawType(flawLevel, seeds)
        }
        val extent = when (RANDOM.nextInt(3)) {
            0 -> FlawExtent.Everywhere()
            1 -> FlawExtent.WholeStep(RANDOM.nextInt(steps.size))
            2 -> {
                val start = RANDOM.nextInt(25)
                val stop = RANDOM.nextInt(start + 10, start + 25)
                FlawExtent.PartialStep(RANDOM.nextInt(steps.size), start, stop)
            }

            else -> FlawExtent.Everywhere()
        }
        return RecipeView(this.identifier, listOf(Flaw(type, extent)))
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