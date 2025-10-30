package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.spawning.BlockDropSpawnConfig
import dev.jsinco.recipes.spawning.MobDropSpawnConfig
import dev.jsinco.recipes.spawning.SpawnConfig
import dev.jsinco.recipes.spawning.conditions.BiomeCondition
import dev.jsinco.recipes.spawning.conditions.WorldCondition
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey

class SpawnConfig : OkaeriConfig() {

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
    var recipeSpawning: ArrayList<SpawnConfig> = arrayListOf(
        SpawnConfig.Builder(SpawnConfig.SpawnConfigType.CONTAINER)
            .addCondition(WorldCondition(listOf(), listOf("norecipesworld")))
            .addToBlacklist("ex")
            .setChance(0.15)
            .setAttempts(1)
            .build(),
        BlockDropSpawnConfig.Builder()
            .addBlock("mushroom_stem")
            .addBlock("red_mushroom_block")
            .addBlock("brown_mushroom_block")
            .addToWhitelist("shroom_vodka")
            .setChance(0.075)
            .setAttempts(1)
            .build(),
        MobDropSpawnConfig.Builder()
            .addEntity("blaze")
            .addCondition(BiomeCondition(listOf("nether_wastes", "crimson_forest")))
            .addToWhitelist("fire_whiskey")
            .setAttempts(1)
            .setChance(1.0)
            .build(),
    )

}