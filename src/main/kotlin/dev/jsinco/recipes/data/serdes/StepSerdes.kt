package dev.jsinco.recipes.data.serdes

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.StepType
import dev.jsinco.recipes.recipe.process.steps.AgeStep
import dev.jsinco.recipes.recipe.process.steps.CookStep
import dev.jsinco.recipes.recipe.process.steps.DistillStep
import dev.jsinco.recipes.recipe.process.steps.MixStep
import net.kyori.adventure.text.minimessage.MiniMessage

object StepSerdes {

    fun serializeStep(step: Step): JsonObject {
        return when (step) {
            is AgeStep -> serializeAgeStep(step)
            is DistillStep -> serializeDistillStep(step)
            is CookStep -> serializeCookStep(step)
            is MixStep -> serializeMixStep(step)
            else -> {
                throw IllegalArgumentException("Step $step is not supported")
            }
        }
    }

    fun serializeMixStep(step: MixStep): JsonObject {
        val output = JsonObject()
        output.addProperty("type", StepType.MIX.name)
        output.addProperty("ticks", step.mixingTicks)
        output.addProperty("cauldron-type", step.cauldronType.name)
        output.add(
            "ingredients",
            serializeIngredients(step.ingredients)
        )
        return output
    }

    private fun serializeIngredients(ingredients: Map<Ingredient, Int>): JsonObject {
        val output = JsonObject()
        for ((key, value) in ingredients) {
            val ingredientData = JsonObject()
            ingredientData.addProperty("amount", value)
            ingredientData.addProperty("display-name", MiniMessage.miniMessage().serialize(key.displayName))
            output.add(key.key, ingredientData)
        }
        return output
    }

    fun serializeCookStep(step: CookStep): JsonObject {
        val output = JsonObject()
        output.addProperty("type", StepType.COOK.name)
        output.addProperty("ticks", step.cookingTicks)
        output.addProperty("cauldron-type", step.cauldronType.name)
        output.add(
            "ingredients",
            serializeIngredients(step.ingredients)
        )
        return output
    }

    fun serializeAgeStep(age: AgeStep): JsonObject {
        val output = JsonObject()
        output.addProperty("type", StepType.AGE.name)
        output.addProperty("ticks", age.agingTicks)
        output.addProperty("barrel-type", age.barrelType.name)
        return output
    }

    fun serializeDistillStep(distill: DistillStep): JsonObject {
        val output = JsonObject()
        output.addProperty("type", StepType.DISTILL.name)
        output.addProperty("runs", distill.count)
        return output
    }

    fun deserializeStep(stepJson: JsonElement): Step? {
        if (stepJson !is JsonObject) {
            return null
        }
        val typeString = stepJson.get("type") ?: return null
        return when (StepType.fromString(typeString.asString)) {
            StepType.MIX -> deserializeMix(stepJson)
            StepType.COOK -> deserializeCook(stepJson)
            StepType.DISTILL -> deserializeDistill(stepJson)
            StepType.AGE -> deserializeAge(stepJson)
        }
    }

    private fun deserializeAge(stepJson: JsonObject): AgeStep? {
        val agingTicks = stepJson.get("ticks")?.asLong ?: return null
        val barrelType = stepJson.get("barrel-type")?.asString ?: return null
        return AgeStep(agingTicks, AgeStep.BarrelType.fromString(barrelType))
    }

    private fun deserializeDistill(stepJson: JsonObject): DistillStep? {
        val runs = stepJson.get("runs")?.asLong ?: return null
        return DistillStep(runs)
    }

    private fun deserializeCook(stepJson: JsonObject): CookStep? {
        val ticks = stepJson.get("ticks")?.asLong ?: return null
        val cauldronType = stepJson.get("cauldron-type")?.asString ?: return null
        val ingredients = stepJson.get("ingredients") ?: return null
        if (ingredients !is JsonObject) {
            return null
        }
        return CookStep(
            ticks,
            CookStep.CauldronType.fromString(cauldronType),
            deserializeIngredients(ingredients) ?: return null
        )
    }

    private fun deserializeMix(stepJson: JsonObject): MixStep? {
        val ticks = stepJson.get("ticks")?.asLong ?: return null
        val cauldronType = stepJson.get("cauldron-type")?.asString ?: return null
        val ingredients = stepJson.get("ingredients") ?: return null
        if (ingredients !is JsonObject) {
            return null
        }
        return MixStep(
            ticks,
            MixStep.CauldronType.fromString(cauldronType),
            deserializeIngredients(ingredients) ?: return null
        )
    }

    private fun deserializeIngredients(stepJson: JsonObject): Map<Ingredient, Int>? {
        return buildMap {
            for (entry in stepJson.asMap().entries) {
                val ingredientData = entry.value
                if (ingredientData !is JsonObject) {
                    return null
                }
                val amount = ingredientData.get("amount")?.asInt ?: return null
                val displayName = ingredientData.get("display-name")?.asString ?: return null
                put(Ingredient(entry.key, MiniMessage.miniMessage().deserialize(displayName)), amount)
            }
        }
    }
}