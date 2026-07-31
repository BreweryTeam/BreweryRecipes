package dev.jsinco.recipes.recipe

enum class FragmentationGroup(val translationKey: String) {
    COMPLETE("breweryrecipes.gui.recipes.name.complete"),
    SLIGHTLY_FRAGMENTED("breweryrecipes.gui.recipes.name.slightly-fragmented"),
    MODERATELY_FRAGMENTED("breweryrecipes.gui.recipes.name.moderately-fragmented"),
    HEAVILY_FRAGMENTED("breweryrecipes.gui.recipes.name.heavily-fragmented"),
    SEVERELY_FRAGMENTED("breweryrecipes.gui.recipes.name.severely-fragmented"),
    MISSING("breweryrecipes.gui.recipes.name.missing");

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
