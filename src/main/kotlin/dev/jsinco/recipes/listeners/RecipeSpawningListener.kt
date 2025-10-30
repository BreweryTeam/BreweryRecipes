package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.spawning.BlockDropSpawnConfig
import dev.jsinco.recipes.spawning.LootSpawnConfig
import dev.jsinco.recipes.spawning.MobDropSpawnConfig
import dev.jsinco.recipes.spawning.SpawnConfig
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Minecart
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class RecipeSpawningListener : Listener {

    private fun getRecipes(spawnConfig: SpawnConfig): MutableList<ItemStack> {
        val attempts = (spawnConfig.attempts ?: 1).coerceAtLeast(1)
        val chance = (spawnConfig.chance ?: 1.0).coerceIn(0.0, 1.0)
        val applicableRecipes = Recipes.recipes().asSequence()
            .filter { spawnConfig.whitelist == null || !spawnConfig.whitelist.contains(it.key) }
            .filter { spawnConfig.blacklist == null || !spawnConfig.blacklist.contains(it.key) }
            .map { it.value }
            .toList()
        if (applicableRecipes.isEmpty()) return mutableListOf()
        val results = mutableListOf<ItemStack>()
        repeat(attempts) {
            if (Math.random() <= chance) {
                results.add(applicableRecipes.random().lootItem())
            }
        }
        return results
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onLootGenerate(event: LootGenerateEvent) {
        for (spawnConfig in Recipes.spawnConfig.recipeSpawning) {
            if (spawnConfig.enabled == false) continue
            if (!conditionsMatch(spawnConfig, event.lootContext.location, event.lootContext.lootedEntity)) continue
            if (spawnConfig is LootSpawnConfig) {
                for (lootTable in spawnConfig.lootTables) {
                    if (lootTable.key != event.lootTable.key) continue
                    event.loot.addAll(getRecipes(spawnConfig))
                }
            } else {
                when (spawnConfig.type) {
                    SpawnConfig.SpawnConfigType.CONTAINER -> {
                        if (event.lootContext.location.block.state !is InventoryHolder) continue
                        event.loot.addAll(getRecipes(spawnConfig))
                    }
                    SpawnConfig.SpawnConfigType.CHEST -> {
                        if (event.lootContext.location.block.type != Material.CHEST) continue
                        event.loot.addAll(getRecipes(spawnConfig))
                    }
                    SpawnConfig.SpawnConfigType.BARREL -> {
                        if (event.lootContext.location.block.type != Material.BARREL) continue
                        event.loot.addAll(getRecipes(spawnConfig))
                    }
                    SpawnConfig.SpawnConfigType.MINECART -> {
                        if (event.lootContext.lootedEntity !is Minecart) continue
                        event.loot.addAll(getRecipes(spawnConfig))
                    }
                    else -> continue
                }
            }
        }
    }

    private fun getRecipe(spawnConfig: SpawnConfig): ItemStack? {
        val attempts = (spawnConfig.attempts ?: 1).coerceAtLeast(1)
        val chance = (spawnConfig.chance ?: 1.0).coerceIn(0.0, 1.0)
        val applicableRecipes = Recipes.recipes().asSequence()
            .filter { spawnConfig.whitelist == null || !spawnConfig.whitelist.contains(it.key) }
            .filter { spawnConfig.blacklist == null || !spawnConfig.blacklist.contains(it.key) }
            .map { it.value }
            .toList()
        if (applicableRecipes.isEmpty()) return null
        var success = false
        repeat(attempts) { if (Math.random() <= chance) success = true }
        if (success) return applicableRecipes.random().lootItem()
        return null
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onFishCatch(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val caught = event.caught ?: return
        if (caught is Item) {
            for (spawnConfig in Recipes.spawnConfig.recipeSpawning) {
                if (spawnConfig.enabled == false) continue
                if (spawnConfig.type != SpawnConfig.SpawnConfigType.FISHING) continue
                if (!conditionsMatch(spawnConfig, event.caught?.location ?: event.player.location, event.caught)) continue
                val recipe = getRecipe(spawnConfig)
                if (recipe != null) caught.itemStack = recipe
            }
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        for (spawnConfig in Recipes.spawnConfig.recipeSpawning) {
            if (spawnConfig.enabled == false || spawnConfig.type != SpawnConfig.SpawnConfigType.MOB_DROP) continue
            val mobConfig = spawnConfig as? MobDropSpawnConfig ?: continue
            if (!mobConfig.contains(entity.type)) continue
            if (!conditionsMatch(mobConfig, event.entity.location, event.entity)) continue

            val extraDrops = getRecipes(spawnConfig)
            if (extraDrops.isNotEmpty()) event.drops.addAll(extraDrops)
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockDrop(event: BlockDropItemEvent) {
        for (spawnConfig in Recipes.spawnConfig.recipeSpawning) {
            if (spawnConfig.enabled == false || spawnConfig.type != SpawnConfig.SpawnConfigType.BLOCK_DROP) continue
            val blockConfig = spawnConfig as? BlockDropSpawnConfig ?: continue
            if (!blockConfig.contains(event.blockState.blockData.material)) continue
            if (!conditionsMatch(blockConfig, event.blockState.location, null)) continue

            val extraDrops = getRecipes(spawnConfig)
            if (extraDrops.isEmpty()) continue

            val loc = event.blockState.location.add(0.5, 0.5, 0.5)
            for (drop in extraDrops) event.blockState.world.dropItemNaturally(loc, drop)
        }
    }

    private fun conditionsMatch(spawnConfig: SpawnConfig, location: Location, entity: Entity? = null): Boolean {
        val context = dev.jsinco.recipes.spawning.conditions.SpawnCondition.SpawnContext(location, entity)
        val conditions = spawnConfig.conditions ?: return true
        return conditions.all { it.matches(context) }
    }

}