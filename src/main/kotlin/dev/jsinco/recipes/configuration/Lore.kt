package dev.jsinco.recipes.configuration

import net.kyori.adventure.text.Component

class Lore(vararg content: Component) {

    val components = content.toList()
}