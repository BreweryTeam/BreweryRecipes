package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import net.kyori.adventure.text.Component

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
    var recipes: MutableMap<String, DetailsEntry> = mutableMapOf()

    class Defaults : OkaeriConfig() {

        @Comment(
            "Controls when the recipe appears in the recipe book:",
            "VISIBLE: Always shown in fragments mode (if the recipe hasn't been learned yet, will show up as an empty bottle by default)",
            "SECRET: Shown after learning the recipe",
            "HIDDEN: Never shown, useful for admin-only recipes"
        )
        var visibility: Visibility = Visibility.SECRET

        @Comment(
            "Hint text displayed in the recipe book.",
            "If the recipe is VISIBLE, this will be shown for unlearned recipes as well."
        )
        var hint: MutableList<Component> = mutableListOf()

        @Comment(
            "A comment describing the effect of this recipe.",
            "If the recipe is VISIBLE, this will be shown for unlearned recipes as well."
        )
        var effect: MutableList<Component> = mutableListOf()

        @Comment("Who created this recipe")
        var author: Component? = null

    }

}
