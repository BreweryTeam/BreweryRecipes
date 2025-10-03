package dev.jsinco.recipes.util

import java.util.logging.Level
import java.util.logging.Logger as JLogger

object Logger {

    private fun logger(): JLogger = JLogger.getLogger("BreweryRecipes")


    fun prefix(s: String, m: String): String {
        val caller = Thread.currentThread().stackTrace[2]
        val className = caller.className.substringAfterLast('.')
        return "[BreweryRecipes $s - $className:${caller.lineNumber}] $m"
    }

    fun log(message: String) {
        logger().log(Level.INFO, prefix("Info", message))
    }

    fun logErr(message: String) {
        logger().log(Level.SEVERE, prefix("Error", message))
    }

    fun logErr(throwable: Throwable) {
        logger().log(Level.SEVERE, prefix("Error", (throwable.message ?: "")), throwable)
    }

    fun logDev(message: String) {
        logger().log(Level.WARNING, prefix("Debug", message))
    }

}
