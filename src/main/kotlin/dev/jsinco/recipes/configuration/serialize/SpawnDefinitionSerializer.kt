package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.spawning.SpawnDefinition
import dev.jsinco.recipes.configuration.spawning.SpawnItemType
import dev.jsinco.recipes.configuration.spawning.ConditionsDefinition
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData

object SpawnDefinitionSerializer : ObjectSerializer<SpawnDefinition> {

    override fun supports(type: Class<*>): Boolean {
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
        data.add("item-type", `object`.itemType)
        data.add("flawless", `object`.flawless)
        `object`.flaws?.let {
            data.add("flaws", it)
        }
        `object`.flawLevelMin?.let { data.add("flaw-level-min", it) }
        `object`.flawLevelMax?.let { data.add("flaw-level-max", it) }
        `object`.itemOverride?.let { data.add("item-override", it) }

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
        val itemType = data.get("item-type", SpawnItemType::class.java) ?: SpawnItemType.SCRIBBLING
        val flawless = data.get("flawless", Boolean::class.java) ?: false
        val flaws = data.getAsList("flaws", RecipeViewCreator.Type::class.java) ?: listOf()
        val flawLevelMin = data.get("flaw-level-min", Double::class.javaObjectType)
        val flawLevelMax = data.get("flaw-level-max", Double::class.javaObjectType)
        val itemOverride = data.get("item-override", ConfigItem::class.java)
        val conditions = data.get("conditions", ConditionsDefinition::class.java)
        val conditionsBlacklist = data.get("condition-blacklist", ConditionsDefinition::class.java)
        val triggers = data.get("triggers", TriggersDefinition::class.java)
        return SpawnDefinition(
            enabled = enabled,
            attempts = attempts,
            chance = chance,
            itemType = itemType,
            recipeWhitelist = whitelist,
            recipeBlacklist = blacklist,
            flawless = flawless,
            flaws = flaws,
            flawLevelMin = flawLevelMin,
            flawLevelMax = flawLevelMax,
            itemOverride = itemOverride,
            conditions = conditions,
            conditionBlacklist = conditionsBlacklist,
            triggers = triggers
        )
    }
}