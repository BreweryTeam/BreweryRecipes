package dev.jsinco.recipes.recipe.flaws

import dev.jsinco.recipes.recipe.flaws.type.FlawType

data class Flaw(val type: FlawType, val config: FlawConfig)
