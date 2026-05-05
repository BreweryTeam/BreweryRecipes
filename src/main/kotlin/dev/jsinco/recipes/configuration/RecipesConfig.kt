package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import java.util.*

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
    @CustomKey("recipe-sort-order")
    var recipeSortOrder: RecipeSortOrder = RecipeSortOrder.AS_PROVIDED

    @Comment(
        "In the fragments book, group recipes by their completeness before applying the sort order?",
        "Complete recipes appear first, followed by slightly fragmented, moderately fragmented,",
        "heavily fragmented, and severely fragmented (each group still sorted among itself)"
    )
    @CustomKey("group-fragments-by-completeness")
    var groupFragmentsByCompleteness: Boolean = true

    @Comment("In the brew notes book, group recipes by their score so the most perfected brews appear first?")
    @CustomKey("group-brew-notes-by-score")
    var groupBrewNotesByScore: Boolean = true

    @Comment("Minimum delay between recipe book opens per player. Set to 0 to disable")
    @CustomKey("open-cooldown-ticks")
    var openCooldownTicks: Long = 10L

    @Comment("Minimum delay between page switches per player. Set to 0 to disable")
    @CustomKey("page-cooldown-ticks")
    var pageCooldownTicks: Long = 3L

    @Comment("Minimum delay between mode switches per player. Set to 0 to disable")
    @CustomKey("mode-switch-cooldown-ticks")
    var modeSwitchCooldownTicks: Long = 5L

    @Comment("Storage settings")
    var storage: StorageConfig = StorageConfig()

    @CustomKey("breweryrecipes-book")
    var book = RecipesBookConfig()
}
