package dev.jsinco.recipes.recipe

enum class FragmentationGroup(val translationKey: String) {
    COMPLETE("breweryrecipes.gui.recipes.name.complete"),
    SLIGHTLY_FRAGMENTED("breweryrecipes.gui.recipes.name.slightly-fragmented"),
    MODERATELY_FRAGMENTED("breweryrecipes.gui.recipes.name.moderately-fragmented"),
    HEAVILY_FRAGMENTED("breweryrecipes.gui.recipes.name.heavily-fragmented"),
    SEVERELY_FRAGMENTED("breweryrecipes.gui.recipes.name.severely-fragmented"),
    UNDISCOVERED("breweryrecipes.gui.recipes.name.undiscovered");

    fun completionState(): RecipeCompletionState {
        return when (this) {
            COMPLETE -> RecipeCompletionState.COMPLETED
            SLIGHTLY_FRAGMENTED, MODERATELY_FRAGMENTED, HEAVILY_FRAGMENTED, SEVERELY_FRAGMENTED -> RecipeCompletionState.PARTIAL
            UNDISCOVERED -> RecipeCompletionState.UNDISCOVERED
        }
    }

    companion object {
        fun of(fragmentation: Double): FragmentationGroup {
            return if (fragmentation <= 0.0) {
                COMPLETE
            } else if (fragmentation < 25.0) {
                SLIGHTLY_FRAGMENTED
            } else if (fragmentation < 50.0) {
                MODERATELY_FRAGMENTED
            } else if (fragmentation < 75.0) {
                HEAVILY_FRAGMENTED
            } else {
                SEVERELY_FRAGMENTED
            }
        }
    }
}
