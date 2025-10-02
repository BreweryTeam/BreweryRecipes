package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.FlawType
import dev.jsinco.recipes.core.flaws.number.InaccuracyFlawType
import dev.jsinco.recipes.core.flaws.text.AmnesiaFlawType
import dev.jsinco.recipes.core.flaws.text.ObfuscationFlawType
import dev.jsinco.recipes.core.flaws.text.OmissionFlawType
import dev.jsinco.recipes.core.flaws.text.SlurringFlawType

object FlawSerdes {


    fun serialize(flaw: Flaw): JsonElement {
        val output = JsonObject()
        output.add("flaw", serializeType(flaw.type))
        output.add("extent", serializeExtent(flaw.extent))
        output.add("seeds", Serdes.serialize(flaw.type.seeds()) {
            JsonPrimitive(it)
        })
        return output
    }

    private fun serializeExtent(extent: FlawExtent): JsonElement {
        val output = JsonObject()
        when (extent) {
            is FlawExtent.Everywhere -> output.addProperty("type", "everywhere")
            is FlawExtent.WholeStep -> {
                output.addProperty("type", "whole_step")
                output.addProperty("step-index", extent.stepIndex)
            }

            is FlawExtent.PartialStep -> {
                output.addProperty("type", "partial_step")
                output.addProperty("step-index", extent.stepIndex)
                output.addProperty("part", extent.part)
            }

            is FlawExtent.ExactIngredient -> {
                output.addProperty("type", "ingredient")
                output.addProperty("step-index", extent.stepIndex)
                output.addProperty("ingredient-key", extent.ingredientKey)
            }

            else -> throw IllegalStateException("Unknown flaw extent")
        }
        return output
    }

    private fun serializeType(flawType: FlawType): JsonElement {
        val output = JsonObject()
        output.addProperty(
            "type",
            when (flawType) {
                is InaccuracyFlawType -> "inaccuracy"
                is AmnesiaFlawType -> "amnesia"
                is ObfuscationFlawType -> "obfuscation"
                is OmissionFlawType -> "omission"
                is SlurringFlawType -> "slurring"
                else -> throw IllegalStateException("Unknown flaw type")
            }
        )
        output.addProperty("intensity", flawType.intensity())
        return output
    }

    fun deserialize(json: JsonElement): Flaw? {
        if (json !is JsonObject) {
            return null
        }
        val typeJson = json.get("flaw")
        val extentJson = json.get("extent")
        val seeds = Serdes.deserialize(json.getAsJsonArray("seeds") ?: JsonArray(), JsonElement::getAsInt)
        val type = deserializeType(typeJson, seeds) ?: return null
        val extent = deserializeExtent(extentJson) ?: return null
        return Flaw(type, extent)
    }

    private fun deserializeExtent(extentJson: JsonElement): FlawExtent? {
        if (extentJson !is JsonObject) {
            return null;
        }
        return when (extentJson.get("type").asString) {
            "everywhere" -> FlawExtent.Everywhere()
            "whole_step" -> FlawExtent.WholeStep(extentJson.get("step-index").asInt)
            "partial_step" -> FlawExtent.PartialStep(
                extentJson.get("step-index").asInt,
                extentJson.get("part").asString
            )

            "ingredient" -> FlawExtent.ExactIngredient(
                extentJson.get("step-index").asInt,
                extentJson.get("ingredient-key").asString
            )

            else -> null
        }
    }

    private fun deserializeType(typeJson: JsonElement, seeds: List<Int>): FlawType? {
        if (typeJson !is JsonObject) {
            return null
        }
        val intensity = typeJson.get("intensity").asDouble
        return when (typeJson.get("type").asString) {
            "inaccuracy" -> InaccuracyFlawType(intensity, seeds)
            "amnesia" -> AmnesiaFlawType(intensity, seeds)
            "obfuscation" -> ObfuscationFlawType(intensity, seeds)
            "omission" -> OmissionFlawType(intensity, seeds)
            "slurring" -> SlurringFlawType(intensity, seeds)
            else -> null
        }
    }
}