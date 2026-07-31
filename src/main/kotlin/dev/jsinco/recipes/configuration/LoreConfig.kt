package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.gui.SectionEntry
import dev.jsinco.recipes.recipe.RecipeCompletionState
import dev.jsinco.recipes.recipe.lore.LoreType
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey

class LoreConfig : OkaeriConfig() {

    @Comment(
        "The list of lore sections in the order they appear in the recipe book.",
        "Use blank lines to separate sections. Adjacent blank lines will be combined into one blank line.",
        "Prefix with a '+' to indent that line.",
        "Duplicates will be ignored.",
        "",
        "STEPS: The steps needed to complete the recipe (fragments mode) or the steps the player performed (brewed mode)",
        "SCORE: The brew's score in 0-5 star format (brewed mode only)",
        "DIFFICULTY: The recipe's difficulty",
        "HINT: Recipe hint defined in details.yml",
        "EFFECT: Brew effect defined in details.yml",
        "AUTHOR: Recipe author defined in details.yml",
        "",
        "List of lore sections that appear in fragments mode:"
    )
    @CustomKey("fragments-sections")
    var fragmentsSections: MutableList<SectionEntry> = mutableListOf(
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.HINT, true),
        SectionEntry(LoreType.DIFFICULTY, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.STEPS, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.EFFECT, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.AUTHOR, true),
        SectionEntry(LoreType.SPACER)
    )

    @Comment("List of lore sections that appear in brewed mode:")
    @CustomKey("brew-notes-sections")
    var brewNotesSections: MutableList<SectionEntry> = mutableListOf(
        SectionEntry(LoreType.SCORE),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.DIFFICULTY, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.STEPS, true),
        SectionEntry(LoreType.SPACER)
    )

    @Comment(
        "When should the recipe difficulty be visible in fragments mode",
        "Any of [completed, partial, undiscovered]"
    )
    @CustomKey("difficulty-visibility")
    var difficultyVisibility: MutableList<RecipeCompletionState> = mutableListOf(
        RecipeCompletionState.COMPLETED,
        RecipeCompletionState.PARTIAL
    )

    @Comment(
        "When should the recipe hints be visible in fragments mode",
        "Any of [completed, partial, undiscovered]"
    )
    @CustomKey("hint-visibility")
    var hintVisibility: MutableList<RecipeCompletionState> = mutableListOf(
        RecipeCompletionState.COMPLETED,
        RecipeCompletionState.PARTIAL,
        RecipeCompletionState.UNDISCOVERED
    )

    @Comment(
        "When should the recipe effect be visible in fragments mode",
        "Any of [completed, partial, undiscovered]"
    )
    @CustomKey("effect-visibility")
    var effectVisibility: MutableList<RecipeCompletionState> = mutableListOf(
        RecipeCompletionState.COMPLETED,
        RecipeCompletionState.PARTIAL
    )

    @Comment(
        "When should the recipe author be visible in fragments mode",
        "Any of [completed, partial, undiscovered]"
    )
    @CustomKey("author-visibility")
    var authorVisibility: MutableList<RecipeCompletionState> = mutableListOf(
        RecipeCompletionState.COMPLETED,
        RecipeCompletionState.PARTIAL,
        RecipeCompletionState.UNDISCOVERED
    )

    @Comment("Insert an empty line between each recipe step?")
    @CustomKey("empty-line-between-steps")
    var emptyLineBetweenSteps: Boolean = true

    @Comment("Number of spaces prepended to every indented lore line")
    @CustomKey("indentation")
    var indentation: Int = 2

    @Comment("Number of spaces appended to every indented lore line")
    @CustomKey("trailing-spaces")
    var trailingSpaces: Int = 2
}
