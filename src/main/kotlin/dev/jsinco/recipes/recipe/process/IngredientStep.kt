package dev.jsinco.recipes.recipe.process

interface IngredientStep : Step {
    
    fun ingredients(): Map<Ingredient, Int>
}