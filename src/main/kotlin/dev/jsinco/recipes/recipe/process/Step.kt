package dev.jsinco.recipes.recipe.process

import net.kyori.adventure.text.Component

interface Step {

    fun getType(): StepType

    fun display(): Component
}