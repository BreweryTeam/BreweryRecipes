package dev.jsinco.recipes.configuration.migration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.migrate.ConfigMigration
import eu.okaeri.configs.migrate.view.RawConfigView

// Cannot create lambdas for ConfigMigration directly because lombok not in PATH
fun interface Migration : ConfigMigration {
   override fun migrate(config: OkaeriConfig, view: RawConfigView): Boolean
}
