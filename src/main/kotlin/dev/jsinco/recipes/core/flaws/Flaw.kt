package dev.jsinco.recipes.core.flaws

import dev.jsinco.recipes.core.flaws.type.FlawType

data class Flaw(val type: FlawType, val config: FlawConfig)
