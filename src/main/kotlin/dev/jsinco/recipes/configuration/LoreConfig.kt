package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.gui.SectionEntry
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
        "HINT: Recipe hint defined in details.yml (fragments mode only)",
        "EFFECT: Brew effect defined in details.yml (fragments mode only)",
        "AUTHOR: Recipe author defined in details.yml (fragments mode only)",
    )
    @CustomKey("sections")
    var sections: MutableList<SectionEntry> = mutableListOf(
        SectionEntry(LoreType.SCORE),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.HINT, true),
        SectionEntry(LoreType.DIFFICULTY, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.STEPS, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.EFFECT, true),
        SectionEntry(LoreType.SPACER),
        SectionEntry(LoreType.AUTHOR, true)
    )

    @Comment("Show the recipe's difficulty in recipe fragments mode?")
    @CustomKey("show-brew-difficulty-in-fragments")
    var showBrewDifficultyInFragments: Boolean = true

    @Comment("Show the recipe's difficulty in brew notes mode?")
    @CustomKey("show-difficulty-in-brew-notes")
    var showDifficultyInBrewNotes: Boolean = false

    @Comment("Show the recipe's difficulty if the recipe has not been seen yet?")
    @CustomKey("show-difficulty-in-missing-recipes")
    var showDifficultyInMissingRecipes: Boolean = false

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
