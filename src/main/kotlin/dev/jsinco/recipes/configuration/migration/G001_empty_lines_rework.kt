package dev.jsinco.recipes.configuration.migration

import eu.okaeri.configs.migrate.ConfigMigrationDsl.*
import eu.okaeri.configs.migrate.builtin.NamedMigration

object G001_empty_lines_rework : NamedMigration(
    "Migrate per-section empty line toggles to general format",
    `when`(
        any(
            exists("recipes.lore.empty-line-above-brew-score"),
            exists("recipes.lore.empty-line-between-name-and-difficulty"),
            exists("recipes.lore.empty-line-above-steps"),
            exists("recipes.lore.empty-line-below-steps")
        ),
        multi(
            // Preserve empty line at top of lore
            move("recipes.lore.empty-line-above-brew-score", "recipes.lore.empty-line-at-top"),
            // There are now many more sections than just name, difficulty, and steps, so configuring empty lines
            // for each combination is impractical.
            // Instead, have one config that controls whether sections are split by empty lines
            Migration { _, view ->
                val empty1 = view.get("recipes.lore.empty-line-between-name-and-difficulty", Boolean::class.java)
                val empty2 = view.get("recipes.lore.empty-line-above-steps", Boolean::class.java)
                supply("recipes.lore.empty-line-between-sections") { empty1 == true || empty2 == true }
                true
            },
            // Preserve empty line at bottom of lore
            move("recipes.lore.empty-line-below-steps", "recipes.lore.empty-line-at-bottom"),
        )
    )
)
