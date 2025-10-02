package dev.jsinco.recipes.core.flaws

interface FlawType {

    fun intensity(): Double

    fun seeds(): List<Int>
}