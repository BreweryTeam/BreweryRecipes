package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.util.function.Function

object Serdes {


    fun <T> serializeCollection(tList: Collection<T>, tConverter: Function<T, JsonElement>): JsonArray {
        val output = JsonArray()
        tList.stream().map(tConverter)
            .forEach { output.add(it) }
        return output
    }


    fun <T> deserializeList(jsonArray: JsonArray, tConverter: Function<JsonElement, T?>): List<T> {
        return jsonArray.asList().asSequence()
            .map { tConverter.apply(it) }
            .filterNotNull()
            .toList()
    }

    fun <T> deserializeSet(jsonArray: JsonArray, tConverter: Function<JsonElement, T?>): Set<T> {
        return jsonArray.asList().asSequence()
            .map { tConverter.apply(it) }
            .filterNotNull()
            .toSet()
    }

}