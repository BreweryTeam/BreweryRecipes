package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey

class RecipesConfig : OkaeriConfig() {

    @Comment("Storage settings")
    var storage: StorageConfig = StorageConfig()

    @CustomKey("recipe-spawning")
    @Comment(
        "Recipes spawn in loot chests randomly throughout the world",
        "Disable this by setting your chance to -1"
    )
    var recipeSpawning: RecipeSpawningSection = RecipeSpawningSection()

    class RecipeSpawningSection : OkaeriConfig() {
        @Comment("The highest value that the random number can generate to. Ex: 100 means a random number between 0 and 100")
        var bound: Int = 100

        @Comment("The chance that a recipe will spawn in a loot chest")
        var chance: Int = 15

        @CustomKey("blacklisted-recipes")
        @Comment("A list of recipes that will not spawn in loot chests")
        var blacklistedRecipes: List<String> = listOf("ex")
    }
}