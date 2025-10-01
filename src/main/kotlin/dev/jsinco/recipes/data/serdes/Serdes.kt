package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.util.function.Function

object Serdes {


    fun <T> serialize(tList: List<T>, tConverter: Function<T, JsonElement>): JsonArray {
        val output = JsonArray()
        tList.stream().map(tConverter)
            .forEach { output.add(it) }
        return output
    }


    fun <T> deserialize(jsonArray: JsonArray, tConverter: Function<JsonElement, T>): List<T> {
        return jsonArray.asList().stream()
            .map(tConverter)
            .toList()
    }
}