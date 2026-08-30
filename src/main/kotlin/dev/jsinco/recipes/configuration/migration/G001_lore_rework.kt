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
            exists("recipes.lore.show-brew-difficulty-in-brew-notes"),
            exists("recipes.lore.empty-line-between-name-and-difficulty"),
            exists("recipes.lore.apply-indentation-to-brew-difficulty"),
            exists("recipes.lore.empty-line-above-steps"),
            exists("recipes.lore.empty-line-below-steps")
        ),
        Migration { _, view ->
            val showScore = view.remove("recipes.lore.show-brew-score") as? Boolean ?: true
            val showDifficultyInFragments = view.remove("recipes.lore.show-brew-difficulty") as? Boolean ?: true
            val showDifficultyInBrewed = view.remove("recipes.lore.show-difficulty-in-brew-notes") as? Boolean ?: false
            val indentScore = view.remove("recipes.lore.apply-indentation-to-brew-score") as? Boolean ?: false
            val indentDifficulty = view.remove("recipes.lore.apply-indentation-to-brew-difficulty") as? Boolean ?: true
            val emptyAboveScore = view.remove("recipes.lore.empty-line-above-brew-score") as? Boolean ?: false
            val emptyAboveDifficulty = view.remove("recipes.lore.empty-line-between-name-and-difficulty") as? Boolean ?: true
            val emptyAboveSteps = view.remove("recipes.lore.empty-line-above-steps") as? Boolean ?: true
            val emptyBelowSteps = view.remove("recipes.lore.empty-line-below-steps") as? Boolean ?: true

            val userProbablyWantsIndents = indentDifficulty
            val userProbablyWantsEmptyLineAtBottom = emptyBelowSteps

            val fragmentsEntries = mutableListOf<SectionEntry>()
            if (showDifficultyInFragments) {
                if (emptyAboveDifficulty) {
                    fragmentsEntries.add(SectionEntry(LoreType.SPACER))
                }
                fragmentsEntries.add(SectionEntry(LoreType.HINT, userProbablyWantsIndents))
                fragmentsEntries.add(SectionEntry(LoreType.DIFFICULTY, indentDifficulty))
            } else {
                if (emptyAboveDifficulty) {
                    fragmentsEntries.add(SectionEntry(LoreType.SPACER))
                }
                fragmentsEntries.add(SectionEntry(LoreType.HINT, userProbablyWantsIndents))
            }
            if (emptyAboveSteps) {
                fragmentsEntries.add(SectionEntry(LoreType.SPACER))
            }
            fragmentsEntries.add(SectionEntry(LoreType.STEPS, true))
            if (emptyBelowSteps) {
                fragmentsEntries.add(SectionEntry(LoreType.SPACER))
            }
            fragmentsEntries.add(SectionEntry(LoreType.EFFECT, userProbablyWantsIndents))
            if (emptyBelowSteps) {
                fragmentsEntries.add(SectionEntry(LoreType.SPACER))
            }
            fragmentsEntries.add(SectionEntry(LoreType.AUTHOR, userProbablyWantsIndents))
            if (userProbablyWantsEmptyLineAtBottom) {
                fragmentsEntries.add(SectionEntry(LoreType.SPACER))
            }
            view.setCollection("recipes.lore.completed-sections", fragmentsEntries, SectionEntry::class.java)
            view.setCollection("recipes.lore.partial-sections", fragmentsEntries, SectionEntry::class.java)

            val undiscoveredEntries = mutableListOf<SectionEntry>()
            if (showDifficultyInFragments) {
                if (emptyAboveDifficulty) {
                    undiscoveredEntries.add(SectionEntry(LoreType.SPACER))
                }
                undiscoveredEntries.add(SectionEntry(LoreType.HINT, userProbablyWantsIndents))
            } else {
                if (emptyAboveDifficulty) {
                    undiscoveredEntries.add(SectionEntry(LoreType.SPACER))
                }
                undiscoveredEntries.add(SectionEntry(LoreType.HINT, userProbablyWantsIndents))
            }
            if (emptyAboveSteps || emptyBelowSteps) {
                undiscoveredEntries.add(SectionEntry(LoreType.SPACER))
            }
            undiscoveredEntries.add(SectionEntry(LoreType.AUTHOR, userProbablyWantsIndents))
            if (userProbablyWantsEmptyLineAtBottom) {
                undiscoveredEntries.add(SectionEntry(LoreType.SPACER))
            }
            view.setCollection("recipes.lore.undiscovered-sections", undiscoveredEntries, SectionEntry::class.java)

            val brewedEntries = mutableListOf<SectionEntry>()
            if (showScore) {
                if (emptyAboveScore) {
                    brewedEntries.add(SectionEntry(LoreType.SPACER))
                }
                brewedEntries.add(SectionEntry(LoreType.SCORE, indentScore))
            }
            if (showDifficultyInBrewed) {
                if (emptyAboveDifficulty) {
                    brewedEntries.add(SectionEntry(LoreType.SPACER))
                }
                brewedEntries.add(SectionEntry(LoreType.DIFFICULTY, indentDifficulty))
            }
            if (emptyAboveSteps) {
                brewedEntries.add(SectionEntry(LoreType.SPACER))
            }
            brewedEntries.add(SectionEntry(LoreType.STEPS, true))
            if (emptyBelowSteps) {
                brewedEntries.add(SectionEntry(LoreType.SPACER))
            }
            view.setCollection("recipes.lore.brew-notes-sections", brewedEntries, SectionEntry::class.java)

            true
        }
    )
)
