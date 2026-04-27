package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import java.util.Locale

class RecipesConfig : OkaeriConfig() {

    @Comment("The language to use when displaying messages and more")
    var language: Locale = Locale.US

    @Comment("Migrate from the old BreweryX-Recipes-Addon?")
    var migrate: Boolean = true

    @Comment(
        "How recipes are ordered in the recipe book:",
        "ALPHABETICAL_IDENTIFIER: sorted alphabetically by recipe identifier",
        "ALPHABETICAL_NAME: sorted by the brew's excellent-quality display name (ignoring color codes/tags)",
        "AS_PROVIDED: keep the order in which the providing brewing plugin relays its recipes"
    )
    var recipeSortOrder: RecipeSortOrder = RecipeSortOrder.AS_PROVIDED

    @Comment("Minimum delay between recipe book opens per player. Set to 0 to disable")
    var openCooldownTicks: Long = 10L

    @Comment("Minimum delay between page switches per player. Set to 0 to disable")
    var pageCooldownTicks: Long = 3L

    @Comment("Storage settings")
    var storage: StorageConfig = StorageConfig()
}