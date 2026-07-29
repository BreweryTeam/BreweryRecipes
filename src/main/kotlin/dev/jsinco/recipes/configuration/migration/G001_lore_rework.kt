package dev.jsinco.recipes.configuration.migration

import dev.jsinco.recipes.configuration.gui.SectionEntry
import dev.jsinco.recipes.recipe.lore.LoreType
import eu.okaeri.configs.migrate.ConfigMigrationDsl.*
import eu.okaeri.configs.migrate.builtin.NamedMigration

object G001_lore_rework : NamedMigration(
    "Migrate per-section empty line and indent toggles to general format",
    `when`(
        any(
            exists("recipes.lore.show-brew-score"),
            exists("recipes.lore.empty-line-above-brew-score"),
            exists("recipes.lore.apply-indentation-to-brew-score"),
            exists("recipes.lore.show-brew-difficulty"),
            exists("recipes.lore.empty-line-between-name-and-difficulty"),
            exists("recipes.lore.apply-indentation-to-brew-difficulty"),
            exists("recipes.lore.empty-line-above-steps"),
            exists("recipes.lore.empty-line-below-steps")
        ),
        Migration { _, view ->
            val showScore = view.remove("recipes.lore.show-brew-score") as? Boolean ?: true
            val indentScore = view.remove("recipes.lore.apply-indentation-to-brew-score") as? Boolean ?: false
            val indentDifficulty = view.remove("recipes.lore.apply-indentation-to-brew-difficulty") as? Boolean ?: true
            val emptyAboveScore = view.remove("recipes.lore.empty-line-above-brew-score") as? Boolean ?: false
            val emptyLineAboveDifficulty = view.remove("recipes.lore.empty-line-between-name-and-difficulty") as? Boolean ?: true
            val emptyAboveSteps = view.remove("recipes.lore.empty-line-above-steps") as? Boolean ?: true
            val emptyBelowSteps = view.remove("recipes.lore.empty-line-below-steps") as? Boolean ?: true

            val userProbablyWantsIndents = indentDifficulty
            val userProbablyWantsEmptyLineAtBottom = (emptyAboveScore || emptyLineAboveDifficulty) && emptyBelowSteps

            val entries = mutableListOf<SectionEntry>()
            if (emptyAboveScore) {
                entries.add(SectionEntry(LoreType.SPACER))
            }
            if (showScore) {
                entries.add(SectionEntry(LoreType.SCORE, indentScore))
            }
            if (emptyLineAboveDifficulty) {
                entries.add(SectionEntry(LoreType.SPACER))
            }
            entries.add(SectionEntry(LoreType.HINT, userProbablyWantsIndents))
            entries.add(SectionEntry(LoreType.DIFFICULTY, indentDifficulty))
            if (emptyAboveSteps) {
                entries.add(SectionEntry(LoreType.SPACER))
            }
            entries.add(SectionEntry(LoreType.STEPS, userProbablyWantsIndents))
            if (emptyBelowSteps) {
                entries.add(SectionEntry(LoreType.SPACER))
            }
            entries.add(SectionEntry(LoreType.EFFECT, userProbablyWantsIndents))
            if (emptyBelowSteps) {
                entries.add(SectionEntry(LoreType.SPACER))
            }
            entries.add(SectionEntry(LoreType.AUTHOR, userProbablyWantsIndents))
            if (userProbablyWantsEmptyLineAtBottom) {
                entries.add(SectionEntry(LoreType.SPACER))
            }
            view.setCollection("recipes.lore.sections", entries, SectionEntry::class.java)

            val showDifficultyInFragments = view.remove("recipes.lore.show-brew-difficulty") as? Boolean ?: true
            view.set("recipes.lore.show-brew-difficulty-in-fragments", showDifficultyInFragments)

            true
        }
    )
)
