package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import java.util.Locale

class RecipesConfig : OkaeriConfig() {

    @Comment("The language to use when displaying messages and more")
    var language: Locale = Locale.US

    @Comment("Migrate from the old BreweryX-Recipes-Addon?")
    var migrate: Boolean = true

    @Comment("Storage settings")
    var storage: StorageConfig = StorageConfig()
}