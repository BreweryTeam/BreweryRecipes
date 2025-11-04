package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.spawning.SpawnDefinition
import dev.jsinco.recipes.configuration.spawning.ConditionsDefinition
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData

object SpawnDefinitionSerializer : ObjectSerializer<SpawnDefinition> {

    override fun supports(type: Class<in SpawnDefinition>): Boolean {
        return SpawnDefinition::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: SpawnDefinition,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        `object`.enabled?.let { data.add("enabled", it) }
        `object`.attempts?.let { data.add("attempts", it) }
        `object`.chance?.let { data.add("chance", it) }

        if (!`object`.recipeWhitelist.isNullOrEmpty()) {
            data.add("recipe-whitelist", `object`.recipeWhitelist)
        }
        if (!`object`.recipeBlacklist.isNullOrEmpty()) {
            data.add("recipe-blacklist", `object`.recipeBlacklist)
        }
        data.add("flawless", `object`.flawless)
        `object`.flaws?.let {
            data.add("flaws", it)
        }

        `object`.conditions?.let {
            data.add("conditions", it)
        }
        `object`.conditionBlacklist?.let {
            data.add("condition-blacklist", it)
        }
        `object`.triggers?.let {
            data.add("triggers", it)
        }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): SpawnDefinition? {
        val enabled = data.get("enabled", Boolean::class.java) ?: true
        val attempts = data.get("attempts", Int::class.javaObjectType)
        val chance = data.get("chance", Double::class.javaObjectType)
        val whitelist = data.getAsList("recipe-whitelist", String::class.java) ?: listOf()
        val blacklist = data.getAsList("recipe-blacklist", String::class.java) ?: listOf()
        val flawless = data.get("flawless", Boolean::class.java) ?: false
        val flaws = data.getAsList("flaws", RecipeViewCreator.Type::class.java) ?: listOf()
        val conditions = data.get("conditions", ConditionsDefinition::class.java)
        val conditionsBlacklist = data.get("condition-blacklist", ConditionsDefinition::class.java)
        val triggers = data.get("triggers", TriggersDefinition::class.java)
        return SpawnDefinition(
            enabled = enabled,
            attempts = attempts,
            chance = chance,
            recipeWhitelist = whitelist,
            recipeBlacklist = blacklist,
            flawless = flawless,
            flaws = flaws,
            conditions = conditions,
            conditionBlacklist = conditionsBlacklist,
            triggers = triggers
        )
    }
}