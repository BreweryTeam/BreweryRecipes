package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey

class LoreConfig : OkaeriConfig() {

    @Comment("Show the brew's quality below its name in brew notes mode?")
    @CustomKey("show-brew-score")
    var showBrewScore: Boolean = true

    @Comment("Insert an empty line before the brew quality line?")
    @CustomKey("empty-line-above-brew-score")
    var emptyLineAboveBrewScore: Boolean = false

    @Comment("Apply indentation and trailing spaces to the brew quality line?")
    @CustomKey("apply-indentation-to-brew-score")
    var applyIndentationToBrewScore: Boolean = false

    @Comment("Show the recipe's difficulty between name and steps?")
    @CustomKey("show-brew-difficulty")
    var showBrewDifficulty: Boolean = true

    @Comment("Insert an empty line between name and difficulty?")
    @CustomKey("empty-line-between-name-and-difficulty")
    var emptyLineAboveBrewDifficulty: Boolean = true

    @Comment("Apply indentation and trailing spaces to the difficulty line?")
    @CustomKey("apply-indentation-to-brew-difficulty")
    var applyIndentationToBrewDifficulty: Boolean = true

    @Comment("Insert an empty line before the first step?")
    @CustomKey("empty-line-above-steps")
    var emptyLineAboveSteps: Boolean = true

    @Comment("Insert an empty line between each recipe step?")
    @CustomKey("empty-line-between-steps")
    var emptyLineBetweenSteps: Boolean = true

    @Comment("Insert an empty line after the last step?")
    @CustomKey("empty-line-below-steps")
    var emptyLineBelowSteps: Boolean = true

    @Comment("Number of spaces prepended to every lore line")
    @CustomKey("indentation")
    var indentation: Int = 2

    @Comment("Number of spaces appended to every lore line")
    @CustomKey("trailing-spaces")
    var trailingSpaces: Int = 2
}
