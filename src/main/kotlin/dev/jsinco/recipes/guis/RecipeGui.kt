package dev.jsinco.recipes.guis

import com.dre.brewery.utility.BUtil
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.Util
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.recipe.Recipe
import dev.jsinco.recipes.recipe.RecipeUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class RecipeGui(player: Player) : InventoryHolder {
    private val inv: Inventory = Bukkit.createInventory(this, Recipes.recipesConfig.gui.size, "Recipes")
    private val recipeGuiItems: MutableList<ItemStack> = mutableListOf()

    init {
        val borderItems: List<Pair<List<Int>, ItemStack>> = GuiItem.getAllGuiBorderItems()
        for (guiItem in borderItems) {
            for (slot in guiItem.first) {
                inv.setItem(slot, guiItem.second)
            }
        }

        val recipes: MutableList<MaybeKnownRecipe> = RecipeUtil.getAllRecipes()
            .map { MaybeKnownRecipe(it, Util.hasRecipePermission(player, it.recipeKey)) }.toMutableList()

        when (Recipes.recipesConfig.gui.sortMethod) {
            SortMethod.ALPHABETICAL -> recipes.sortBy {
                ChatColor.stripColor(RecipeUtil.parseRecipeName(it.recipe.name).lowercase())
            }
            SortMethod.DEFINITION -> {}
        }
        // sortBy is stable, the recipes will stay sorted alphabetically if enabled
        when (Recipes.recipesConfig.gui.unknownRecipeSortMethod) {
            UnknownRecipeSortMethod.KNOWN_FIRST -> recipes.sortByDescending{ it.known }
            UnknownRecipeSortMethod.MIXED -> {}
            UnknownRecipeSortMethod.UNKNOWN_FIRST -> recipes.sortBy{ it.known }
        }

        var knownRecipes = 0
        for (maybeKnownRecipe in recipes) {
            recipeGuiItems.add(GuiItem.createRecipeGuiItem(maybeKnownRecipe.recipe, maybeKnownRecipe.known)).also {
                if (maybeKnownRecipe.known) knownRecipes++
            }
        }

        val totalRecipesItem = GuiItem.getTotalRecipesItem(knownRecipes, recipes.size)
        for (slot in totalRecipesItem.first) {
            inv.setItem(slot, totalRecipesItem.second)
        }
    }

    data class MaybeKnownRecipe(val recipe: Recipe, val known: Boolean)

    //
    val paginatedGui: PaginatedGui = PaginatedGui(BUtil.color(Recipes.recipesConfig.gui.title), inv, recipeGuiItems, Recipes.recipesConfig.gui.items.recipeGuiItem.slots)

    init {
        val arrowItems = GuiItem.getPageArrowItems()
        for (page in paginatedGui.pages) {
            if (paginatedGui.indexOf(page) != 0) {
                for (slot in arrowItems.first.first) {
                    page.setItem(slot, arrowItems.first.second)
                }

            }
            if (paginatedGui.indexOf(page) != paginatedGui.size - 1) {
                for (slot in arrowItems.second.first) {
                    page.setItem(slot, arrowItems.second.second)
                }
            }
        }
    }

    fun openRecipeGui(viewer: Player) {
        viewer.openInventory(paginatedGui.getPage(0))
    }



    override fun getInventory(): Inventory {
        return inv
    }

}
