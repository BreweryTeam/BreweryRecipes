package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

class LoreConfig : OkaeriConfig() {

    @Comment("Insert an empty line between each recipe step")
    var emptyLineBetweenSteps: Boolean = true

    @Comment("Insert an empty line before the first step")
    var emptyLineAtStart: Boolean = true

    @Comment("Insert an empty line after the last step")
    var emptyLineAtEnd: Boolean = true

    @Comment("Number of spaces prepended to every lore line")
    var indentation: Int = 2

    @Comment("Number of spaces appended to every lore line")
    var trailingSpaces: Int = 2
}
