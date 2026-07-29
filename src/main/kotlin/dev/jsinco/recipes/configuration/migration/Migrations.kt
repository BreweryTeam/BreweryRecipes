package dev.jsinco.recipes.configuration.migration

import eu.okaeri.configs.migrate.ConfigMigration

object Migrations {
    fun guiMigrations(): Array<ConfigMigration> = arrayOf(
        G001_empty_lines_rework
    )
}
