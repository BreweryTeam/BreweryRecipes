package dev.jsinco.recipes.spawning.conditions

class BiomeCondition(
    private val whitelist: List<String>? = null,
    private val blacklist: List<String>? = null
) : SpawnCondition {

    override fun matches(context: SpawnCondition.SpawnContext): Boolean {
        val biome = context.location.block.biome
        val id = biome.key().asString().lowercase()
        val shortId = id.removePrefix("minecraft:")

        if (!whitelist.isNullOrEmpty()) {
            val allowed = whitelist.any {
                val key = it.lowercase().removePrefix("minecraft:")
                id.contains(key) || shortId == key
            }
            if (!allowed) return false
        }

        if (!blacklist.isNullOrEmpty()) {
            val blocked = blacklist.any {
                val key = it.lowercase().removePrefix("minecraft:")
                id.contains(key) || shortId == key
            }
            if (blocked) return false
        }

        return true
    }

    override fun toString(): String {
        return "BiomeCondition(whitelist=$whitelist, blacklist=$blacklist)"
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>("type" to "biome")
        whitelist?.takeIf { it.isNotEmpty() }?.let { map["whitelist"] = it }
        blacklist?.takeIf { it.isNotEmpty() }?.let { map["blacklist"] = it }
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any>): BiomeCondition {
            val whitelist = (map["whitelist"] as? List<*>)?.filterIsInstance<String>()
            val blacklist = (map["blacklist"] as? List<*>)?.filterIsInstance<String>()
            return BiomeCondition(whitelist, blacklist)
        }
    }
}
