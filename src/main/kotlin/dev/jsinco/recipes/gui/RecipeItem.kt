package dev.jsinco.recipes.gui

interface RecipeItem : GuiItem {

    override fun type(): String {
        return "recipe"
    }
}