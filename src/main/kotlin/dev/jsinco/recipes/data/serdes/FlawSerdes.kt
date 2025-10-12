package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.type.*

object FlawSerdes {

    fun serializeFlawBundle(bundle: FlawBundle): JsonElement {
        return Serdes.serialize(bundle.flaws, this::serializeFlaw)
    }

    fun deserializeFlawBundle(json: JsonElement): FlawBundle? {
        if (json !is JsonArray) return null
        return FlawBundle(Serdes.deserialize(json, this::deserializeFlaw))
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

            is FlawExtent.StepRange -> {
                output.addProperty("type", "partial_step")
                output.addProperty("step-index", extent.stepIndex)
                output.addProperty("start", extent.start)
                output.addProperty("stop", extent.stop)
            }

            is FlawExtent.AfterPoint -> {
                output.addProperty("type", "after")
                output.addProperty("step-index", extent.stepIndex)
                output.addProperty("start", extent.start)
            }

            else -> throw IllegalStateException("Unknown flaw extent")
        }
        return output
    }

    private fun deserializeFlawExtent(json: JsonElement): FlawExtent? {
        if (json !is JsonObject) return null
        return when (json.get("type").asString) {
            "everywhere" -> FlawExtent.Everywhere
            "whole_step" -> FlawExtent.WholeStep(json.get("step-index").asInt)
            "partial_step" -> FlawExtent.StepRange(
                json.get("step-index").asInt,
                json.get("start").asInt,
                json.get("stop").asInt
            )

            "after" -> FlawExtent.AfterPoint(
                json.get("step-index").asInt,
                json.get("start").asInt
            )

            else -> null
        }
    }

    private fun serializeFlawType(flawType: FlawType): JsonElement {
        val output = JsonObject()
        when (flawType) {
            is InaccuracyFlawType -> output.addProperty("type", "inaccuracy")
            is ReplacementFlawType -> {
                output.addProperty("type", "replacement")
                output.addProperty("chars", flawType.replacement)
            }

            is ObfuscationFlawType -> output.addProperty("type", "obfuscation")
            is SlurringFlawType -> output.addProperty("type", "slurring")
            is CorrectionFlawType -> output.addProperty("type", "correction")
            else -> throw IllegalStateException("Unknown flaw type")
        }
        return output
    }

    private fun deserializeFlawType(json: JsonElement): FlawType? {
        if (json !is JsonObject) return null
        return when (json.get("type").asString) {
            "inaccuracy" -> InaccuracyFlawType
            "replacement" -> {
                val replacement = json.get("chars") ?: return null
                ReplacementFlawType(replacement.asString)
            }

            "obfuscation" -> ObfuscationFlawType
            "slurring" -> SlurringFlawType
            "correction" -> CorrectionFlawType
            else -> null
        }
    }
}
