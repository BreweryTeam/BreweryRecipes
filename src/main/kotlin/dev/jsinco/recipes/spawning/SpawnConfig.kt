package dev.jsinco.recipes.spawning

import dev.jsinco.recipes.spawning.conditions.SpawnCondition

open class SpawnConfig(
    val type: SpawnConfigType,
    val enabled: Boolean? = null,
    val attempts: Int? = null,
    val chance: Double? = null,
    val whitelist: List<String>? = null,
    val blacklist: List<String>? = null,
    val conditions: List<SpawnCondition>? = null
) {
    open class Builder(protected val type: SpawnConfigType) {
        protected var enabled: Boolean? = null
        protected var attempts: Int? = null
        protected var chance: Double? = null
        protected var whitelist: MutableList<String> = mutableListOf()
        protected var blacklist: MutableList<String> = mutableListOf()
        protected var conditions: MutableList<SpawnCondition> = mutableListOf()

        fun setEnabled(enabled: Boolean) = apply { this.enabled = enabled }
        fun setAttempts(attempts: Int) = apply { this.attempts = attempts }
        fun setChance(chance: Double) = apply { this.chance = chance }
        fun setWhitelist(list: MutableList<String>) = apply { this.whitelist = list }
        fun addToWhitelist(value: String) = apply { this.whitelist.add(value) }
        fun setBlacklist(list: MutableList<String>) = apply { this.blacklist = list }
        fun addToBlacklist(value: String) = apply { this.blacklist.add(value) }
        fun setConditions(list: MutableList<SpawnCondition>) = apply { this.conditions = list }
        fun addCondition(condition: SpawnCondition) = apply { this.conditions.add(condition) }

        open fun build() = SpawnConfig(
            type = type,
            enabled = enabled,
            attempts = attempts,
            chance = chance,
            whitelist = if (whitelist.isEmpty()) null else whitelist,
            blacklist = if (blacklist.isEmpty()) null else blacklist,
            conditions = if (conditions.isEmpty()) null else conditions
        )
    }

    enum class SpawnConfigType {
        CONTAINER, CHEST, BARREL, MINECART, FISHING, MOB_DROP, BLOCK_DROP, LOOT
    }

    companion object {
        fun builder(type: SpawnConfigType) = Builder(type)
    }
}
