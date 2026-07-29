package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

class DetailsConfig : OkaeriConfig() {

    @Comment("Default for recipes not in this file")
    var defaults: Defaults = Defaults()

    @Comment(
        "Recipe metadata configuration",
        "",
        "An example recipe with all the parameters:",
        "my-example-brew:",
        "  visibility: VISIBLE",
        "  hint:",
        "  - \"Boil some wheat, let it age\"",
        "  - \"This classic is all the rage!\"",
        "  effect:",
        "  - \"Makes you drunk!\"",
        "  author: \"Brewery Team\""
    )
    var recipes: MutableMap<String, RecipeDetailsEntry> = mutableMapOf()

    class Defaults : OkaeriConfig() {

        @Comment(
            "Whether this recipe is hidden from the recipe book",
            "VISIBLE: Always shown",
            "SECRET: Shown after learning the recipe",
            "HIDDEN: Never shown"
        )
        var visibility: Visibility = Visibility.SECRET

        @Comment("Hint text displayed in the recipe book")
        var hint: MutableList<String> = mutableListOf()

        @Comment("A comment describing the effect of this recipe")
        var effect: MutableList<String> = mutableListOf()

        @Comment("Who created this recipe")
        var author: String? = null

    }

}
