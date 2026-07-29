package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.configuration.DetailsConfig
import dev.jsinco.recipes.configuration.Visibility

data class RecipeDetails(
    val visibility: Visibility,
    val hint: List<String>,
    val effect: List<String>,
    val author: String?
) {
    companion object {
        fun fromConfig(config: DetailsConfig, recipeKey: String): RecipeDetails {
            val metadata = config.recipes[recipeKey]
            return if (metadata != null) {
                RecipeDetails(
                    metadata.visibility ?: config.defaults.visibility,
                    metadata.hint ?: config.defaults.hint,
                    metadata.effect ?: config.defaults.effect,
                    metadata.author ?: config.defaults.author
                )
            } else {
                RecipeDetails(
                    config.defaults.visibility,
                    config.defaults.hint,
                    config.defaults.effect,
                    config.defaults.author
                )
            }
        }
    }
}
