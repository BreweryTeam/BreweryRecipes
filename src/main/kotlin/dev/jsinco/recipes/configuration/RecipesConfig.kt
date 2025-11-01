package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey

class RecipesConfig : OkaeriConfig() {

    @Comment("Storage settings")
    var storage: StorageConfig = StorageConfig()
}