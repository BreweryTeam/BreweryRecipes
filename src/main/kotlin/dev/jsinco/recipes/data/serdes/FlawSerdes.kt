package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonElement
import dev.jsinco.recipes.core.flaws.Flaw

object FlawSerdes {


    fun serialize(flaw: Flaw): JsonElement {
        val type = flaw.type
        val extent = flaw.extent
    }

    fun deserialize(jsonElement: JsonElement): Flaw {

    }
}