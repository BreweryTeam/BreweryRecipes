package dev.jsinco.recipes.util

object ClassUtil {


    fun exists(className: String): Boolean {
        try {
            Class.forName(className)
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }
}