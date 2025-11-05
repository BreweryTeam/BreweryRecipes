package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.configuration.spawning.SpawnDefinition
import dev.jsinco.recipes.configuration.spawning.triggers.InventoryFillTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.LootSpawnTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.MobDropTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.PremadeTrigger
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.block.Container
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.loot.LootTable

class RecipeSpawningListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onLootGenerate(event: LootGenerateEvent) {
        for (spawnDefinition in Recipes.spawnConfig.recipeSpawning) {
            if (spawnDefinition.enabled == false) {
                continue
            }
            if (!conditionsMatch(spawnDefinition, event.lootContext.location)) {
                continue
            }
            if (spawnDefinition.triggers?.asList()?.any {
                    when (it) {
                        is LootSpawnTrigger -> it.lootTables.contains(event.lootTable)
                        is InventoryFillTrigger -> event.inventoryHolder is Container
                        is MobDropTrigger -> event.entity?.type?.let(it.entities::contains) ?: false
                        else -> false
                    }
                } ?: false) {
                event.loot.addAll(spawnDefinition.generateItems())
                continue
            }
            if (spawnDefinition.triggers?.premadeTrigger?.any {
                    when (it) {
                        PremadeTrigger.MINECART -> event.entity?.type?.equals(EntityType.MINECART) ?: false
                        else -> false
                    }
                } ?: false) {
                event.loot.addAll(spawnDefinition.generateItems())
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onFishCatch(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val caught = event.caught ?: return
        if (caught !is Item) {
            return
        }
        for (spawnDefinition in Recipes.spawnConfig.recipeSpawning) {
            if (spawnDefinition.enabled == false) {
                continue
            }
            if (!conditionsMatch(
                    spawnDefinition,
                    event.caught?.location ?: event.player.location
                )
            ) {
                continue
            }
            if (spawnDefinition.triggers?.premadeTrigger?.contains(PremadeTrigger.FISHING) ?: false
                || spawnDefinition.triggers?.lootSpawnTrigger?.lootTables
                    ?.map(LootTable::key)
                    ?.map(Key::value)
                    ?.any {
                        it.contains("fishing")
                    } ?: false
            ) {
                spawnDefinition.generateItem()?.let {
                    caught.itemStack = it
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        for (spawnDefinition in Recipes.spawnConfig.recipeSpawning) {
            if (spawnDefinition.enabled == false) continue
            if (!conditionsMatch(spawnDefinition, event.entity.location)) continue
            if (spawnDefinition.triggers?.mobDropTrigger?.entities?.contains(entity.type) ?: false ||
                spawnDefinition.triggers?.lootSpawnTrigger?.lootTables?.any {
                    it.key.value() == "entities/${event.entity.type.key.value()}"
                } ?: false
            ) {
                event.drops.addAll(spawnDefinition.generateItems())
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockDrop(event: BlockDropItemEvent) {
        for (spawnDefinition in Recipes.spawnConfig.recipeSpawning) {
            if (spawnDefinition.enabled == false) continue
            if (!conditionsMatch(spawnDefinition, event.blockState.location)) continue
            if (spawnDefinition.triggers?.blockDropTrigger?.blocks?.contains(event.blockState.type.asBlockType()!!) ?: false ||
                spawnDefinition.triggers?.lootSpawnTrigger?.lootTables?.any { it.key.value() == "blocks/${event.blockState.type.key.value()}" } ?: false
            ) {
                val loc = event.blockState.location.add(0.5, 0.5, 0.5)
                spawnDefinition.generateItems().forEach {
                    event.blockState.world.dropItemNaturally(loc, it)
                }
            }
        }
    }

    private fun conditionsMatch(spawnConfig: SpawnDefinition, location: Location): Boolean {
        if (spawnConfig.conditionBlacklist?.matchesLocation(location) ?: false) {
            return false
        }
        if (spawnConfig.conditions == null) {
            return true
        }
        return spawnConfig.conditions.matchesLocation(location)
    }

}