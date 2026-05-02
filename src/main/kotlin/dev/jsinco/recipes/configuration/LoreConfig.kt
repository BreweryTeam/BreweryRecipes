package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

class LoreConfig : OkaeriConfig() {

    @Comment("Show the recipe's difficulty between name and steps?")
    var showDifficulty: Boolean = true

    @Comment("Insert an empty line between name and difficulty?")
    var emptyLineAboveDifficulty: Boolean = true

    @Comment("Insert an empty line before the first step?")
    var emptyLineAboveSteps: Boolean = true

    @Comment("Insert an empty line between each recipe step?")
    var emptyLineBetweenSteps: Boolean = true

    @Comment("Insert an empty line after the last step?")
    var emptyLineBelowSteps: Boolean = true

    @Comment("Number of spaces prepended to every lore line")
    var indentation: Int = 2

    @Comment("Number of spaces appended to every lore line")
    var trailingSpaces: Int = 2
}
