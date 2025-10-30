package dev.jsinco.recipes.spawning.conditions

class WorldCondition(
    private val whitelist: List<String>? = null,
    private val blacklist: List<String>? = null
) : SpawnCondition {

    override fun matches(context: SpawnCondition.SpawnContext): Boolean {
        val world = context.location.world ?: return false
        val worldName = world.name.lowercase()

        if (!whitelist.isNullOrEmpty()) {
            val allowed = whitelist.any { worldName == it.lowercase() }
            if (!allowed) return false
        }

        if (!blacklist.isNullOrEmpty()) {
            val blocked = blacklist.any { worldName == it.lowercase() }
            if (blocked) return false
        }

        return true
    }

    override fun toString(): String {
        return "WorldCondition(whitelist=$whitelist, blacklist=$blacklist)"
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>("type" to "world")
        whitelist?.takeIf { it.isNotEmpty() }?.let { map["whitelist"] = it }
        blacklist?.takeIf { it.isNotEmpty() }?.let { map["blacklist"] = it }
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any>): WorldCondition {
            val whitelist = (map["whitelist"] as? List<*>)?.filterIsInstance<String>()
            val blacklist = (map["blacklist"] as? List<*>)?.filterIsInstance<String>()
            return WorldCondition(whitelist, blacklist)
        }
    }
}
