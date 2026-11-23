package dev.jsinco.recipes.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.flattener.FlattenerListener
import net.kyori.adventure.text.format.Style

object ComponentUtil {

    // Splits a component into separate lines at every <newline> or \n
    fun splitLines(component: Component): List<Component> {
        val lines = mutableListOf<TextComponent.Builder>(Component.text())
        val styles = ArrayDeque<Style>().apply { addLast(Style.empty()) }

        ComponentFlattener.basic().flatten(component, object : FlattenerListener {
            override fun pushStyle(style: Style) {
                styles.addLast(styles.last().merge(style))
            }

            override fun component(text: String) {
                text.split('\n').forEachIndexed { index, part ->
                    if (index > 0) lines.add(Component.text())
                    if (part.isNotEmpty()) lines.last().append(Component.text(part, styles.last()))
                }
            }

            override fun popStyle(style: Style) {
                styles.removeLast()
            }
        })
        return lines.map { it.build() }
    }
}
