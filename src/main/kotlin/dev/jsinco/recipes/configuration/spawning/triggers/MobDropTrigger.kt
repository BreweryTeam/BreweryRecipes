package dev.jsinco.recipes.configuration.spawning.triggers

import org.bukkit.entity.EntityType

class MobDropTrigger(
    vararg val entities: EntityType
) : SpawnTrigger
