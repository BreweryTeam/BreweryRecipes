package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.FlawType
import dev.jsinco.recipes.core.flaws.number.InaccuracyFlawType
import dev.jsinco.recipes.core.flaws.text.AmnesiaFlawType
import dev.jsinco.recipes.core.flaws.text.ObfuscationFlawType
import dev.jsinco.recipes.core.flaws.text.OmissionFlawType
import dev.jsinco.recipes.core.flaws.text.SlurringFlawType

object FlawSerdes {

    fun serializeFlawBundle(bundle: FlawBundle): JsonElement {
        val output = JsonArray()
        bundle.flaws.forEach { flaw ->
            output.add(serializeFlaw(flaw))
        }
        return output
    }
    fun deserializeFlawBundle(json: JsonElement): FlawBundle? {
        if (json !is JsonArray) return null
        val flaws = json.mapNotNull { deserializeFlaw(it) }
        return FlawBundle(flaws)
    }

    fun serializeFlaw(flaw: Flaw): JsonElement {
        val output = JsonObject()
        output.add("type", serializeFlawType(flaw.type))
        output.add("config", serializeFlawConfig(flaw.config))
        return output
    }
    fun deserializeFlaw(json: JsonElement): Flaw? {
        if (json !is JsonObject) return null
        val type = deserializeFlawType(json.get("type")) ?: return null
        val config = deserializeFlawConfig(json.get("config")) ?: return null
        return Flaw(type, config)
    }

    private fun serializeFlawConfig(config: FlawConfig): JsonElement {
        val output = JsonObject()
        output.add("extent", serializeFlawExtent(config.extent))
        output.addProperty("seed", config.seed)
        output.addProperty("intensity", config.intensity)
        return output
    }
    private fun deserializeFlawConfig(json: JsonElement): FlawConfig? {
        if (json !is JsonObject) return null
        val extent = deserializeFlawExtent(json.get("extent")) ?: return null
        val seed = json.get("seed").asInt
        val intensity = json.get("intensity").asDouble
        return FlawConfig(extent, seed, intensity)
    }

    private fun serializeFlawExtent(extent: FlawExtent): JsonElement {
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
                output.addProperty("start", extent.start)
                output.addProperty("stop", extent.stop)
            }
            else -> throw IllegalStateException("Unknown flaw extent")
        }
        return output
    }
    private fun deserializeFlawExtent(json: JsonElement): FlawExtent? {
        if (json !is JsonObject) return null
        return when (json.get("type").asString) {
            "everywhere" -> FlawExtent.Everywhere()
            "whole_step" -> FlawExtent.WholeStep(json.get("step-index").asInt)
            "partial_step" -> FlawExtent.PartialStep(
                json.get("step-index").asInt,
                json.get("start").asInt,
                json.get("stop").asInt
            )
            else -> null
        }
    }

    private fun serializeFlawType(flawType: FlawType): JsonElement {
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
        return output
    }
    private fun deserializeFlawType(json: JsonElement): FlawType? {
        if (json !is JsonObject) return null
        return when (json.get("type").asString) {
            "inaccuracy" -> InaccuracyFlawType()
            "amnesia" -> AmnesiaFlawType()
            "obfuscation" -> ObfuscationFlawType()
            "omission" -> OmissionFlawType()
            "slurring" -> SlurringFlawType()
            else -> null
        }
    }
}
