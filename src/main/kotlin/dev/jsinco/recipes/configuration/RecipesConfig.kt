package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.Material

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

    @CustomKey("recipe-book-item")
    var recipeBookItem: ConfigItemSection = ConfigItemSection.Builder()
        .material(Material.BOOK)
        .name("&6&lRecipe Book")
        .lore("&7Right click to open", "&7your recipe book!")
        .glint(true)
        .build()


    @CustomKey("recipe-item")
    @Comment(
        "The physical recipe item that will spawn in loot chests",
        "PLACEHOLDERS: %recipe% - The recipe's name"
    )
    var recipeItem = ConfigItemSection.Builder()
        .material(Material.PAPER)
        .name("&#F7FFC9%recipe% &fRecipe")
        .lore("&7Right-Click to redeem", "&7this recipe!")
        .glint(true)
        .build()


    @Comment(
        "Full example Gui Border Item",
        "item:",
        "  material: SHORT_GRASS",
        "  slots: [ 0 ]",
        "  display_name: 'example item'",
        "  lore: [ 'example lore', 'another line' ]",
        "  glint: true",
        "  custom_model_data: -1",
    )
    var gui: GuiSection = GuiSection()

    class GuiSection : OkaeriConfig() {
        var title = "&#f670f1&lR&#dd7af6&le&#c584fa&lc&#ac8eff&li&#9c92ff&lp&#8d96ff&le&#7d9aff&ls"
        var size = 54


        @CustomKey("sort-method")
        @Comment(
            "Determines how recipes are sorted in the Gui",
            "When using ALPHABETICAL, recipes are sorted by their name alphabetically, case insensitive",
            "When using DEFINITION, recipes are sorted in the same order they are defined in recipes.yml"
        )

        var items = GuiItemsSection()

        class GuiItemsSection : OkaeriConfig() {

            @CustomKey("recipe-gui-item")
            @Comment(
                "The item that will be used to show all the recipes the player has",
                "When material is set to POTION material the RGB/Color specified for the potion in the config will be used",
                "PLACEHOLDERS: %recipe% - The recipe's name",
                "               %difficulty% - The difficulty of the recipe",
                "               %cooking_time% - The cooking time of the recipe",
                "               %distill_runs% - The distill runs of the recipe",
                "               %age% - The age of the recipe",
                "               %barrel_type% - The barrel wood type of the recipe",
                "               %ingredients% - The ingredients of the recipe using the ingredient-format"
            )
            var recipeGuiItem = RecipeGuiItemSection()

            class RecipeGuiItemSection : ConfigRecipeItem, OkaeriConfig() {
                @CustomKey("ingredient-format")
                @Comment(
                    "The format for the ingredients",
                    "PLACEHOLDERS: %amount% - The amount of the ingredient",
                    "               %ingredient% - The ingredient's name"
                )
                override var ingredientFormat = " &#F7FFC9%amount%x &f%ingredient%"
                override var material = Material.POTION
                var slots = listOf(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

                @CustomKey("display_name")
                override var name = "&#F7FFC9%recipe% &fRecipe"
                override var lore = listOf(
                    "&fDifficulty&7: &#F7FFC9%difficulty%",
                    "&fCooking Time&7: &#F7FFC9%cooking_time%m",
                    "&fDistill Runs&7: &#F7FFC9%distill_runs%",
                    "&fAge&7: &#F7FFC9%age%yrs &f(Minecraft days)",
                    "&fBarrel Type&7: &#F7FFC9%barrel_type%",
                    "",
                    "&7%lore%",
                    "",
                    "&fIngredients&7:",
                    "%ingredients%"
                )
                override var glint = true

                @CustomKey("use-recipe-custom-model-data")
                @Comment("If true, the custom model data will be set to the recipe's custom model data")
                override var useRecipeCustomModelData = false
            }

            @CustomKey("unknown-recipe")
            @Comment(
                "The item shown when a player does not know a recipe",
                "Supports the same placeholders as recipe-gui-item"
            )
            var unknownRecipe = UnknownRecipeGuiItemSection()

            class UnknownRecipeGuiItemSection : ConfigRecipeItem, OkaeriConfig() {
                @CustomKey("ingredient-format")
                @Comment("The format for the ingredients")
                override var ingredientFormat = " &#F7FFC9%amount%x &f%ingredient%"

                @Comment("Set to AIR to disable completely and only have *known* recipes show up")
                override var material = Material.AIR

                @CustomKey("display_name")
                override var name = "&#f498f6??? Recipe"
                override var lore = listOf("&7This recipe is unknown to you")
                override var glint = false

                @CustomKey("use-recipe-custom-model-data")
                @Comment("If true, the custom model data will be set to the recipe's custom model data")
                override var useRecipeCustomModelData = false
            }


            @CustomKey("total_recipes")
            @Comment("PLACEHOLDERS: %total_recipes% - The total amount of recipes the player has out of the total recipes in Brewery's config")
            var totalRecipes = ConfigItemSection.Builder()
                .material(Material.BOOK)
                .slots(49)
                .name("&#f498f6Total Recipes&7: &e%total_recipes%")
                .glint(true)
                .build()


            @CustomKey("next_page")
            var nextPage = ConfigItemSection.Builder()
                .material(Material.ARROW)
                .slots(50)
                .name("&#f498f6Next Page")
                .lore()
                .build()


            @CustomKey("previous_page")
            var previousPage = ConfigItemSection.Builder()
                .material(Material.ARROW)
                .slots(48)
                .name("&#f498f6Previous Page")
                .lore()
                .build()


            @CustomKey("border-items")
            @Comment("Border items")
            var borderItems = BorderItemsSection()

            class BorderItemsSection : OkaeriConfig() {
                var border1 = ConfigItemSection.Builder()
                    .material(Material.GREEN_STAINED_GLASS_PANE)
                    .slots(0, 8, 45, 53)
                    .name("&0")
                    .build()
                var border2 = ConfigItemSection.Builder()
                    .material(Material.SHORT_GRASS)
                    .slots(1, 7, 46, 52)
                    .name("&0")
                    .build()
                var border3 = ConfigItemSection.Builder()
                    .material(Material.FERN)
                    .slots(2, 6, 47, 51)
                    .name("&0")
                    .build()
                var border4 = ConfigItemSection.Builder()
                    .material(Material.PINK_TULIP)
                    .slots(3, 5, 48, 50)
                    .name("&0")
                    .build()
                var border5 = ConfigItemSection.Builder()
                    .material(Material.LILY_PAD)
                    .slots(4, 49)
                    .name("&0")
                    .build()
            }
        }
    }

    @Comment("PLACEHOLDERS: %recipe% the name of the recipe.")
    var messages = MessagesSection()

    class MessagesSection : OkaeriConfig() {
        @CustomKey("already-learned")
        var alreadyLearned = "&rYou already know this recipe!"

        var learned = "&rYou have learned the '&#F7FFC9%recipe%&r' recipe!"

        @CustomKey("not-learned")
        @Comment("Used when a player tries to brew a recipe they do not know (requires: `require-recipe-permission-to-brew` to be enabled)")
        var notLearned = "&cYou do not know the recipe for this potion!"
    }
}