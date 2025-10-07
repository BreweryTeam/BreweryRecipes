package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.jsinco.recipes.core.flaws.drunkentext.DrunkenTextReplacement

object DrunkenTextSerdes {


    fun deserialize(json: JsonElement): DrunkenTextReplacement? {
        if (json !is JsonObject) {
            return null
        }
        val to = json.get("to").asString
        val from = json.get("from").asString.toRegex()
        val intensity = if (json.has("alcohol")) json.get("alcohol").asDouble else 5.0
        val percentage = json.get("percentage").asDouble
        return DrunkenTextReplacement(from, intensity, percentage / 100, to, 0.2)
    }
}