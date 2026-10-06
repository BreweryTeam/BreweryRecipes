package dev.jsinco.recipes.configuration.migration

import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.gui.GuiOverride
import dev.jsinco.recipes.gui.GuiItem
import eu.okaeri.configs.migrate.ConfigMigrationDsl.all
import eu.okaeri.configs.migrate.ConfigMigrationDsl.exists
import eu.okaeri.configs.migrate.ConfigMigrationDsl.not
import eu.okaeri.configs.migrate.ConfigMigrationDsl.`when`
import eu.okaeri.configs.migrate.builtin.NamedMigration
import org.bukkit.Material

object G002_group_override : NamedMigration(
    "Add VIEW_GROUPS override",
    `when`(
        all(
            exists("overrides"),
            not(exists("defaultView"))
        ),
        Migration { _, view ->
            val overrides = view.getAsList("overrides", GuiOverride::class.java)
            if (overrides.any { it.type == GuiItem.Type.VIEW_GROUPS }) {
                return@Migration false
            }

            overrides.add(GuiOverride.Builder()
                .pos("4")
                .item(
                    ConfigItem.Builder().material(Material.BREWING_STAND)
                        .name("<gray>Groups")
                        .build()
                ).type(GuiItem.Type.VIEW_GROUPS)
                .build())

            view.setCollection("overrides", overrides, GuiOverride::class.java)
            true
        }
    )
)
