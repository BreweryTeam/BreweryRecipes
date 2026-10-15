package dev.jsinco.recipes.util

object ClassUtil {

    fun exists(className: String): Boolean {
        try {
            Class.forName(className)
            return true
        } catch (_: ClassNotFoundException) {
            return false
        }
    }

    fun exists(className: String, methodName: String): Boolean {
        return try {
            Class.forName(className).methods.any { m -> m.name == methodName }
        } catch (_: ClassNotFoundException) {
            false
        }
    }

}