package dev.jsinco.recipes.configuration

class ConfigItemCollection(vararg content: ConfigItem) {

    val content = content.toList()
}