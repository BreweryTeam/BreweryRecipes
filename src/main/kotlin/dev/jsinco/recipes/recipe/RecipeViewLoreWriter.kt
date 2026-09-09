package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.integration.BrewingIntegration
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.flaws.FlawExtent
import dev.jsinco.recipes.recipe.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.recipe.flaws.FlawTextModifications
import dev.jsinco.recipes.recipe.flaws.type.FlawType
import dev.jsinco.recipes.recipe.lore.AuthorSection
import dev.jsinco.recipes.recipe.lore.DifficultySection
import dev.jsinco.recipes.recipe.lore.EffectSection
import dev.jsinco.recipes.recipe.lore.HintSection
import dev.jsinco.recipes.recipe.lore.ScoreSection
import dev.jsinco.recipes.recipe.lore.LoreType
import dev.jsinco.recipes.recipe.lore.RecipeLoreLines
import dev.jsinco.recipes.recipe.lore.SpacerSection
import dev.jsinco.recipes.recipe.lore.StepsSection
import dev.jsinco.recipes.util.TranslationUtil
import dev.jsinco.recipes.util.ext.removeAdjacentWhere
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object RecipeViewLoreWriter {

    var cookingMinuteTicks = 20L * 60L
    var agingYearTicks = 20L * 60L * 20L

    // no touchy
    var version: Int = 0
    fun bumpVersion() {
        version++
    }

    fun writeLore(recipeDisplay: RecipeDisplay, brewingIntegration: BrewingIntegration, isBrewNote: Boolean = false): List<Component>? {
        cookingMinuteTicks = brewingIntegration.cookingMinuteTicks()
        agingYearTicks = brewingIntegration.agingYearTicks()

        val recipe = BreweryRecipes.brewingIntegration.getRecipe(recipeDisplay.recipeKey()) ?: return null
        val details = RecipeDetails.fromConfig(BreweryRecipes.detailsConfig, recipe.identifier)
        val loreConfig = BreweryRecipes.guiConfig.recipes.lore

        val sections = if (isBrewNote) {
            loreConfig.brewNotesSections
        } else when (recipeDisplay.fragmentationGroup().completionState()) {
            RecipeCompletionState.COMPLETED -> loreConfig.completedSections
            RecipeCompletionState.PARTIAL -> loreConfig.partialSections
            RecipeCompletionState.UNDISCOVERED -> loreConfig.undiscoveredSections
        }
        val recipeView = recipeDisplay.generateView()
        val loreComponentsBySection = sections.mapNotNull { sectionEntry ->
            when (sectionEntry.type) {
                LoreType.STEPS -> recipeView?.let { view ->
                    val stepsToRender = recipeDisplay.displaySteps() ?: recipe.steps
                    StepsSection(stepsToRender, view, isBrewNote)
                }
                LoreType.SCORE -> if (recipeDisplay is BreweryRecipe) ScoreSection(recipeDisplay) else null
                LoreType.DIFFICULTY -> DifficultySection(recipe, recipeView)
                LoreType.HINT -> HintSection(details.hint)
                LoreType.EFFECT -> EffectSection(details.effect)
                LoreType.AUTHOR -> AuthorSection(details.author)
                LoreType.SPACER -> SpacerSection
            }?.let { section -> section to sectionEntry.indent }
        }.mapNotNull { (section, indent) ->
            section.lore(indent)?.let { section.type() to it }
        }.removeAdjacentWhere { (type, _) ->
            type == LoreType.SPACER
        }
        if (loreComponentsBySection.all { (type, _) -> type == LoreType.SPACER }) {
            return emptyList()
        }
        return loreComponentsBySection.flatMap { (_, lore) -> lore }
    }

    fun applyAffixes(line: Component): Component {
        val loreConfig = BreweryRecipes.guiConfig.recipes.lore
        val prefix = if (loreConfig.indentation > 0) Component.text(" ".repeat(loreConfig.indentation)) else null
        val suffix = if (loreConfig.trailingSpaces > 0) Component.text(" ".repeat(loreConfig.trailingSpaces)) else null
        if (prefix != null || suffix != null) {
            var out = line
            if (prefix != null) out = prefix.append(out)
            if (suffix != null) out = out.append(suffix)
            return out
        }
        return line
    }

    fun applyFlaws(
        component: Component,
        stepIndex: Int,
        flaws: List<Flaw>,
        reveals: List<Set<Int>>,
        revealIndex: Int = stepIndex,
        onPositionObscured: ((Int) -> Unit)? = null
    ): Component {
        if (flaws.isEmpty()) return component
        val base = resolveTranslatablesForMutation(component)
        val revealed = revealFilter(reveals, revealIndex)
        val textModifications = compileTextModifications(base, stepIndex, flaws)
            .map { it.key to it.value.withMatching { idx -> revealed == null || revealed.contains(idx) } }
            .toMap()
        var output = base
        var offsets = mapOf<Int, Int>()
        for (flaw in flaws) {
            val textModification = textModifications[flaw] ?: continue
            output = FlawTextModificationWriter.process(output, textModification, flaw, offsets, onPositionObscured)
            offsets = textModification.offsets(offsets)
        }
        return output
    }

    // Positions a view still obscures on one line (null when it obscures everything its flaws appy to)
    private fun revealFilter(invertedReveals: List<Set<Int>>, revealIndex: Int): Set<Int>? {
        if (invertedReveals.isEmpty()) return null
        return invertedReveals.getOrNull(revealIndex) ?: emptySet()
    }

    private fun compileTextModifications(
        step: Component,
        stepIndex: Int,
        flaws: List<Flaw>
    ): Map<Flaw, FlawTextModifications> {
        val allTextModifications = mutableMapOf<Flaw, FlawTextModifications>()
        if (flaws.isEmpty()) {
            return allTextModifications
        }
        val flawPositions = mutableListOf<Int>()
        for (flaw in flaws) {
            if (flawApplies(stepIndex, flaw)) {
                val session = FlawType.ModificationFindSession(stepIndex, flaw.config) {
                    !flawPositions.contains(it)
                }
                val textModifications = flaw.type.findFlawModifications(step, session)
                flawPositions.addAll(
                    textModifications.modifiedPoints
                        .keys
                )
                allTextModifications[flaw] = textModifications
            }
        }
        return allTextModifications
            .filter { !it.value.modifiedPoints.isEmpty() && !it.value.modifiedPoints.all { entry -> entry.value is FlawTextModifications.NoModification } }
    }

    fun estimateFragmentation(recipeView: RecipeView): Double {
        val recipe = BreweryRecipes.brewingIntegration.getRecipe(recipeView.recipeIdentifier) ?: return 100.0
        if (recipe.steps.isEmpty()) return 0.0

        var fragmentation = 0.0

        RecipeLoreLines.steps(recipe.steps, false)
            .filter { it.type == RecipeLoreLines.LineType.STEP }
            .forEach { stepLine ->
                val base = resolveTranslatablesForMutation(stepLine.component)
                val approxBaseLength = PlainTextComponentSerializer.plainText().serialize(base).length
                val revealed = revealFilter(recipeView.invertedReveals, stepLine.revealIndex)
                val modifications = compileTextModifications(base, stepLine.stepIndex, recipeView.flaws)
                    .map {
                        it.key to it.value.withMatching { pos ->
                            revealed == null || revealed.contains(pos)
                        }
                    }.toMap()
                if (modifications.isEmpty()) {
                    return@forEach
                }
                fragmentation += modifications.values.sumOf { it.intensity(approxBaseLength) }
            }

        return fragmentation / recipe.steps.size * 100.0
    }

    fun clearRedundantFlaws(view: RecipeView, thresholdPercent: Double = 15.0): RecipeView {
        val applicableFlaws = mutableSetOf<Flaw>()
        val recipe = BreweryRecipes.brewingIntegration.getRecipe(view.recipeIdentifier) ?: return view
        RecipeLoreLines.all(recipe).forEach { line ->
            compileTextModifications(
                resolveTranslatablesForMutation(line.component),
                line.stepIndex,
                view.flaws
            ).keys.forEach { applicableFlaws.add(it) }
        }

        val newFlaws = view.flaws.filter { applicableFlaws.contains(it) }
        val pct = estimateFragmentation(view)
        return if (pct < thresholdPercent) {
            RecipeView(view.recipeIdentifier, emptyList(), emptyList())
        } else {
            RecipeView(view.recipeIdentifier, newFlaws, view.invertedReveals)
        }
    }

    fun mergeFlaws(base: RecipeView, toSubtract: RecipeView): RecipeView {
        val recipe = BreweryRecipes.brewingIntegration.getRecipe(base.recipeIdentifier) ?: return base
        val invertedReveals = RecipeLoreLines.all(recipe)
            .sortedBy { it.revealIndex }
            .map { line ->
                obscuredPositions(base, line) intersect obscuredPositions(toSubtract, line)
            }
        return RecipeView(
            base.recipeIdentifier, base.flaws, invertedReveals
        )
    }

    private fun obscuredPositions(recipeView: RecipeView, flawableLine: RecipeLoreLines.FlawableLine): Set<Int> {
        val obscured = mutableSetOf<Int>()
        applyFlaws(
            flawableLine.component,
            flawableLine.stepIndex,
            recipeView.flaws,
            recipeView.invertedReveals,
            flawableLine.revealIndex
        ) { position -> obscured.add(position) }
        return obscured
    }

    private fun flawApplies(stepIndex: Int, flaw: Flaw): Boolean {
        return when (flaw.config.extent) {
            is FlawExtent.Everywhere -> true
            is FlawExtent.WholeStep -> stepIndex == flaw.config.extent.stepIndex
            is FlawExtent.StepRange -> stepIndex == flaw.config.extent.stepIndex
            is FlawExtent.AfterPoint -> stepIndex >= flaw.config.extent.stepIndex
            else -> false
        }
    }

    private fun resolveTranslatablesForMutation(node: Component): Component {
        val mappedChildren = node.children().map { resolveTranslatablesForMutation(it) }
        val withChildren = node.children(mappedChildren)

        return when (withChildren) {
            is TranslatableComponent -> {
                val rendered = TranslationUtil.render(withChildren)
                if (rendered !is TranslatableComponent) {
                    resolveTranslatablesForMutation(rendered)
                } else {
                    Component.text(
                        BreweryRecipes.instance.translator?.findClientSideTranslation(rendered.key()) ?: rendered.key()
                    ).children(rendered.children())
                        .style(rendered.style())
                }
            }

            else -> withChildren
        }
    }

}