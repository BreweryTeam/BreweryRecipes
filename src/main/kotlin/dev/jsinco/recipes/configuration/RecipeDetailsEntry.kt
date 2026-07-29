package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig

class RecipeDetailsEntry : OkaeriConfig() {
    var visibility: Visibility? = null
    var hint: MutableList<String>? = null
    var effect: MutableList<String>? = null
    var author: String? = null
}
