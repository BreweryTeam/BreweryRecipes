package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey

class LoreConfig : OkaeriConfig() {

    @Comment("Insert an empty line at the top?")
    @CustomKey("empty-line-at-top")
    var emptyLineAtTop: Boolean = false

    @Comment("Insert an empty line between each lore section?")
    @CustomKey("empty-line-between-sections")
    var emptyLineBetweenSections: Boolean = true

    @Comment("Show the brew's quality below its name in brew notes mode?")
    @CustomKey("show-brew-score")
    var showBrewScore: Boolean = true

    @Comment("Apply indentation and trailing spaces to the brew quality line?")
    @CustomKey("apply-indentation-to-brew-score")
    var applyIndentationToBrewScore: Boolean = false

    @Comment("Show the recipe's difficulty in recipe fragments mode?")
    @CustomKey("show-brew-difficulty")
    var showBrewDifficulty: Boolean = true

    @Comment("Show the recipe's difficulty in brew notes mode?")
    @CustomKey("show-difficulty-in-brew-notes")
    var showDifficultyInBrewNotes: Boolean = false

    @Comment("Show the recipe's difficulty if the recipe has not been seen yet?")
    @CustomKey("show-difficulty-in-missing-recipes")
    var showDifficultyInMissingRecipes: Boolean = false

    @Comment("Apply indentation and trailing spaces to the difficulty line?")
    @CustomKey("apply-indentation-to-brew-difficulty")
    var applyIndentationToBrewDifficulty: Boolean = true

    @Comment("Insert an empty line between each recipe step?")
    @CustomKey("empty-line-between-steps")
    var emptyLineBetweenSteps: Boolean = true

    @Comment("Insert an empty line at the bottom?")
    @CustomKey("empty-line-at-bottom")
    var emptyLineAtBottom: Boolean = true

    @Comment("Number of spaces prepended to every lore line")
    @CustomKey("indentation")
    var indentation: Int = 2

    @Comment("Number of spaces appended to every lore line")
    @CustomKey("trailing-spaces")
    var trailingSpaces: Int = 2
}
