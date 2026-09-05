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

    @Comment(
        "Whether players see a message and sound when they complete a recipe for the first time.",
        "Only available for TheBrewingProject."
    )
    @CustomKey("show-recipe-complete-message")
    var showRecipeCompleteMessage: Boolean = true

    @Comment(
        "Whether players see a message and sound when they perfect (5-stars) a recipe for the first time.",
        "Only available for TheBrewingProject."
    )
    @CustomKey("show-recipe-perfect-message")
    var showRecipePerfectMessage: Boolean = true

    @Comment(
        "Whether players learn part of the true recipe by creating brews.",
        "As players create higher and higher-quality brews, they learn more and more of the true recipe.",
        "Only available for TheBrewingProject."
    )
    @CustomKey("incremental-learning")
    var incrementalLearning: Boolean = false

    @Comment(
        "If incremental-learning is true, determines how much of the true recipe each quality level reveals:",
        "learningCurve > 1: Low-quality brews reveal little, high-quality brews reveal a lot",
        "learningCurve < 1: Low-quality brews reveal a lot, high-quality brews reveal a little",
        "learningCurve <= 0: Creating a brew immediately reveals as much as possible",
        "The recipe's fragmentation percentage is calculated with the following formula:",
        "fragmentation = min + (max - min) * (1 - halfStars/10) ^ learningCurve"
    )
    @CustomKey("learning-curve")
    var learningCurve: Double = 2.0

    @Comment(
        "If incremental-learning is true, when the player creates a 0-star brew,",
        "determines how fragmented the learned recipe is.",
        "Ranges from 0.0 to 100.0."
    )
    @CustomKey("max-learning-fragmentation")
    var maxLearningFragmentation: Double = 80.0

    @Comment(
        "If incremental-learning is true, when the player creates a 5-star brew,",
        "determines how fragmented the learned recipe is.",
        "Ranges from 0.0 to max-learning-fragmentation."
    )
    @CustomKey("min-learning-fragmentation")
    var minLearningFragmentation: Double = 0.0

    @Comment("Storage settings")
    var storage: StorageConfig = StorageConfig()

    @CustomKey("recipes-book")
    var book = RecipesBookConfig()
}
