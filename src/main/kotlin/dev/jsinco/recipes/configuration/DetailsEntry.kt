package dev.jsinco.recipes.configuration

import net.kyori.adventure.text.Component

class DetailsEntry(
    var visibility: Visibility?,
    var hint: MutableList<Component>?,
    var effect: MutableList<Component>?,
    var author: Component?
)
