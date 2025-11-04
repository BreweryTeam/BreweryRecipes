package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.spawning.SpawnDefinition
import dev.jsinco.recipes.configuration.spawning.triggers.BlockDropTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.InventoryFillTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.LootSpawnTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.MobDropTrigger
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.entity.EntityType
import org.bukkit.event.inventory.InventoryType

class SpawnConfig : OkaeriConfig() {

    //TODO: Rewrite comment
    @CustomKey("recipe-spawning")
    @Comment(
        "+-------------------------------------------------------------------------------------------+",
        "| This file is for specifying when and how recipes should randomly appear in your world(s). |",
        "+-------------------------------------------------------------------------------------------+",
        " ",
        "Available 'type' values:",
        "  CONTAINER   - When any container (chest, barrel, etc.) generates loot.",
        "  CHEST       - When a chest specifically generates loot.",
        "  BARREL      - When a barrel specifically generates loot.",
        "  MINECART    - When a chest minecart generates loot (in mineshafts).",
        "  FISHING     - When a player catches an item via fishing.",
        "  MOB_DROP    - When a mob dies and drops items.",
        "  BLOCK_DROP  - When a block is broken and drops items.",
        "  LOOT        - When a specific loot table generates items (e.g. structure loot).",
        " ",
        "Common settings (available for all types):",
        "  enabled: <true|false>   - Optional. Enables or disables this spawn entry.",
        "  attempts: <number>      - How many times to attempt spawning a recipe per event.",
        "  chance: <0.0–1.0>       - Probability for each attempt (1.0 = 100% = always).",
        "  whitelist: <list>       - Only recipes with matching keys will be considered.",
        "  blacklist: <list>       - Recipes with these keys will not be spawned.",
        " ",
        "Type-specific options:",
        "  MOB_DROP:",
        "    entities: <list>      - Entity types or tags (e.g. 'zombie', '#skeletons') that can drop recipes.",
        " ",
        "  BLOCK_DROP:",
        "    blocks: <list>        - Block types or tags (e.g. 'ancient_debris', '#leaves') that can drop recipes.",
        " ",
        "  LOOT:",
        "    lootTables: <list>    - Loot-table-keys (e.g. 'minecraft:chests/trial_chambers/intersection_barrel') that can generate",
        "                            recipes as additional loot (only works for loot-tables that trigger the LootGenerateEvent).",
        " ",
        "Conditions (optional, shared across all types):",
        "  Each condition defines additional restrictions under which recipes may spawn.",
        " ",
        "  Available condition types:",
        "    - type: biome",
        "      whitelist: [biome1, biome2, ...]  - Only allow in these biomes (works with- and without 'minecraft:' prefix).",
        "      blacklist: [biome3, ...]          - No recipes will be spawned in these biomes.",
        " ",
        "    - type: world",
        "      whitelist: [world, world_nether]  - Only allow in these worlds.",
        "      blacklist: [world_the_end]        - Disallow these worlds.",
        " ",
        "Your time to shine:"
    )
    var recipeSpawning: ArrayList<SpawnDefinition> = arrayListOf(
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.15,
            flaw = RecipeViewCreator.Type.DRUNK,
            triggers = listOf(
                LootSpawnTrigger.fromStrings(
                    "chests/shipwreck_supply",
                    "chests/abandoned_mineshaft"
                )
            )
        ),
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.15,
            flaw = RecipeViewCreator.Type.UNCERTAIN,
            triggers = listOf(
                LootSpawnTrigger.fromStrings(
                    "chests/village/village_fisher",
                    "chests/village/shepherd",
                    "chests/village/village_temple",
                    "chests/village/tannery",
                    "chests/village/village_plains_house",
                    "chests/village/village_savanna_house"
                )
            )
        ),
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.15,
            flaw = RecipeViewCreator.Type.ENCRYPTED,
            triggers = listOf(
                InventoryFillTrigger(
                    InventoryType.CHEST,
                    InventoryType.BARREL
                )
            ),
            conditions = listOf(
                WorldCondition(
                    "the_end"
                )
            )
        ),
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.075,
            triggers = listOf(
                InventoryFillTrigger(
                    InventoryType.CHEST,
                    InventoryType.BARREL
                )
            ),
            conditionBlacklist = listOf(
                WorldCondition(
                    "overworld"
                )
            )
        ),
        SpawnDefinition(
            recipeWhitelist = listOf("shroom_vodka"),
            attempts = 1,
            chance = 0.0125,
            flaw = RecipeViewCreator.Type.DRUNK,
            triggers = listOf(
                BlockDropTrigger(
                    "mushroom_stem",
                    "red_mushroom_block",
                    "brown_mushroom_block"
                )
            )
        ),
        SpawnDefinition(
            recipeWhitelist = listOf("fire_whiskey"),
            attempts = 1,
            chance = 0.075,
            flaw = RecipeViewCreator.Type.ENCRYPTED,
            triggers = listOf(
                MobDropTrigger(
                    EntityType.BLAZE
                )
            ),
            conditions = listOf(
                BiomeCondition(
                    "nether_wastes",
                    "crimson_forest"
                )
            )
        )
    )

}