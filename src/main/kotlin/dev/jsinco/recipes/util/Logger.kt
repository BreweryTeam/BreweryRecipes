package dev.jsinco.recipes.util

import java.util.logging.Level
import java.util.logging.Logger as JLogger

class Logger private constructor() {

    companion object {

        fun log(message: String) {
            val caller = Thread.currentThread().stackTrace[2]
            val className = caller.className.substringAfterLast('.')
            val prefixedMessage = "[BreweryRecipes Info - $className:${caller.lineNumber}] $message"
            logger().log(Level.INFO, prefixedMessage)
        }

        fun logErr(message: String) {
            val caller = Thread.currentThread().stackTrace[2]
            val className = caller.className.substringAfterLast('.')
            val prefixedMessage = "[BreweryRecipes Error - $className:${caller.lineNumber}] $message"
            logger().log(Level.SEVERE, prefixedMessage)
        }

        fun logErr(throwable: Throwable) {
            val caller = Thread.currentThread().stackTrace[2]
            val className = caller.className.substringAfterLast('.')
            val prefix = "[BreweryRecipes Error - $className:${caller.lineNumber}] "
            logger().log(Level.SEVERE, prefix + (throwable.message ?: ""), throwable)
        }

        fun logDev(message: String) {
            val caller = Thread.currentThread().stackTrace[2]
            val className = caller.className.substringAfterLast('.')
            val prefixedMessage = "[BreweryRecipes DevDebug - $className:${caller.lineNumber}] $message"
            logger().log(Level.WARNING, prefixedMessage)
        }

        private fun logger(): JLogger =
            JLogger.getLogger("BreweryRecipes")
    }
}
