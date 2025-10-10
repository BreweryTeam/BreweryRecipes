package dev.jsinco.recipes.configuration.gui

enum class GuiBorderType(vararg val positions: Int) {
    UPPER(0,1,2,3,4,5,6,7,8),
    LOWER(53,52,51,50,49,48,47,46,45),
    LEFT(9,18,27,36),
    RIGHT(17,26,35,44)
}