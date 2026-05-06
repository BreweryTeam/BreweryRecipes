package dev.jsinco.recipes.recipe

import com.google.common.collect.ImmutableList
import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.steps.AgeStep
import dev.jsinco.recipes.recipe.process.steps.CookStep
import dev.jsinco.recipes.recipe.process.steps.DistillStep
import dev.jsinco.recipes.recipe.process.steps.MixStep
import dev.jsinco.recipes.util.PdcKeys
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

data class BreweryRecipe(
    val identifier: String,
    val steps: List<Step>,
    val difficulty: Double,
    val score: Double = 0.0
) : RecipeDisplay {

    fun lootItem(base: ItemStack): ItemStack {
        base.setData(
            DataComponentTypes.CUSTOM_NAME,
            TranslationUtil.render(
                Component.translatable("breweryrecipes.spawning.item.name.completed")
                    .colorIfAbsent(NamedTextColor.WHITE)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        )
        base.setData(
            DataComponentTypes.LORE, ItemLore.lore(
                listOf(Component.translatable("breweryrecipes.spawning.item.lore"))
                    .map { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) }
                    .map { it.colorIfAbsent(NamedTextColor.GRAY) }
                    .map(TranslationUtil::render)
            )
        )
        base.editPersistentDataContainer { it.set(PdcKeys.RECIPE_KEY, PersistentDataType.STRING, identifier) }
        return base
    }

    fun lootItem(base: ItemStack, recipeViewCreatorType: RecipeViewCreator.Type): ItemStack {
        val output = lootItem(base)
        output.editPersistentDataContainer {
            it.set(
                PdcKeys.FLAW_KEY,
                PersistentDataType.STRING,
                recipeViewCreatorType.name.lowercase(Locale.ROOT)
            )
        }
        output.setData(
            DataComponentTypes.CUSTOM_NAME,
            TranslationUtil.render(
                Component.translatable(recipeViewCreatorType.lootTranslationKey)
                    .colorIfAbsent(NamedTextColor.WHITE)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        )
        return output
    }

    fun generateCompletedView(): RecipeView {
        return RecipeView.of(this.identifier, listOf())
    }

    fun generate(expectedFlawLevel: Double): RecipeView {
        val collection = RecipeViewCreator.Type.entries.random()
        return generate(expectedFlawLevel, collection)
    }

    fun generate(expectedFlawLevel: Double, flawViewType: RecipeViewCreator.Type): RecipeView {
        return flawViewType.recipeViewCreator.create(this, expectedFlawLevel)
    }

    override fun recipeKey(): String = identifier

    override fun toLore(): List<Component> {
        return RecipeViewLoreWriter.writeLore(
            generateCompletedView(),
            BreweryRecipes.brewingIntegration,
            steps,
            isBrewNote = true
        ) ?: emptyList()
    }

    override fun displayName(brewDisplayName: Component): Component {
        return brewDisplayName
    }

    override fun scoreEquivalent(): Double {
        return BreweryRecipes.brewingIntegration.score(this)
    }

    class Builder(private val identifier: String) {
        private val stepsBuilder = ImmutableList.Builder<Step>()
        private var difficulty: Double = 0.0
        private var score: Double = 0.0

        fun mix(ticks: Long, cauldronType: String?, ingredients: Map<Ingredient, Int>) = apply {
            stepsBuilder.add(MixStep(ticks, MixStep.CauldronType.fromString(cauldronType), ingredients))
        }

        fun cook(ticks: Long, cauldronType: String?, ingredients: Map<Ingredient, Int>) = apply {
            stepsBuilder.add(CookStep(ticks, CookStep.CauldronType.fromString(cauldronType), ingredients))
        }

        fun distill(count: Long) = apply { stepsBuilder.add(DistillStep(count)) }

        fun age(ticks: Long, barrelType: String) = apply {
            stepsBuilder.add(AgeStep(ticks, AgeStep.BarrelType.fromString(barrelType)))
        }

        fun difficulty(value: Double) = apply { difficulty = value }

        fun score(value: Double) = apply { score = value }

        fun build() = BreweryRecipe(identifier, stepsBuilder.build(), difficulty, score)
    }

}