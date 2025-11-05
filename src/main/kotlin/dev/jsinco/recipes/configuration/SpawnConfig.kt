package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.spawning.ConditionsDefinition
import dev.jsinco.recipes.configuration.spawning.SpawnDefinition
import dev.jsinco.recipes.configuration.spawning.triggers.*
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.block.Biome
import org.bukkit.block.BlockType
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
        "Common settings:",
        "  enabled: <true|false>   - Optional. Enables or disables this spawn entry.",
        "  attempts: <number>      - How many times to attempt spawning a recipe per event.",
        "  chance: <0.0–1.0>       - Probability for each attempt (1.0 = 100% = always).",
        "  whitelist: <list>       - Only recipes with matching keys will be considered.",
        "  blacklist: <list>       - Recipes with these keys will not be spawned.",
        "  flaws: <list>           - Optional. The flaws to choose to apply to a recipe",
        "  flawless: <true|false>  - Optional. True if there's no flaw on this recipe",
        " ",
        "Triggers options:",
        "  premade: <fishing|barrel|chest|minecart> - Optional. Premade triggers",
        "  loot: <list>                             - Optional. Loot table triggers, see https://minecraft.wiki/w/Loot_table",
        "  entities: <list>                         - Optional. A list of entities that should drop recipe loot when killed",
        "  blocks: <list>                           - Optional. A list of blocks that should drop recipe loot when broken",
        "  inventories: <list>                      - Optional. A list of inventory types that should populate recipe loot",
        " ",
        "Conditions (optional):",
        "  biomes: <list> - Optional. Biomes the event has to occurred in",
        "  worlds: <list> - Optional. Worlds the event has to occurred in",
        " ",
        "Condition Blacklist (optional)",
        "  biomes: <list> - Optional. Biomes the event has to not occurred in",
        "  worlds: <list> - Optional. Worlds the event has to not occurred in",
        " ",
        "Your time to shine:"
    )
    var recipeSpawning: ArrayList<SpawnDefinition> = arrayListOf(
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.15,
            flaws = listOf(RecipeViewCreator.Type.DRUNK),
            triggers = TriggersDefinition(
                lootSpawnTrigger = LootSpawnTrigger.fromStrings(
                    "chests/shipwreck_supply",
                    "chests/abandoned_mineshaft"
                ),
                premadeTrigger = listOf(PremadeTrigger.FISHING)
            )
        ),
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.15,
            flaws = listOf(RecipeViewCreator.Type.UNCERTAIN),
            triggers = TriggersDefinition(
                lootSpawnTrigger = LootSpawnTrigger.fromStrings(
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
            flaws = listOf(RecipeViewCreator.Type.ENCRYPTED),
            triggers = TriggersDefinition(
                inventoryFillTrigger = InventoryFillTrigger(
                    InventoryType.CHEST,
                    InventoryType.BARREL
                )
            ),
            conditions = ConditionsDefinition(
                worldCondition = listOf("the_end")
            )
        ),
        SpawnDefinition(
            recipeBlacklist = listOf("ex"),
            attempts = 1,
            chance = 0.075,
            triggers = TriggersDefinition(
                inventoryFillTrigger = InventoryFillTrigger(
                    InventoryType.CHEST,
                    InventoryType.BARREL
                )
            ),
            conditionBlacklist = ConditionsDefinition(
                worldCondition = listOf("overworld")
            )
        ),
        SpawnDefinition(
            recipeWhitelist = listOf("shroom_vodka"),
            attempts = 1,
            chance = 0.0125,
            flaws = listOf(RecipeViewCreator.Type.DRUNK),
            triggers = TriggersDefinition(
                blockDropTrigger = BlockDropTrigger(
                    BlockType.MUSHROOM_STEM,
                    BlockType.RED_MUSHROOM_BLOCK,
                    BlockType.BROWN_MUSHROOM_BLOCK
                )
            )
        ),
        SpawnDefinition(
            recipeWhitelist = listOf("fire_whiskey"),
            attempts = 1,
            chance = 0.075,
            flaws = listOf(RecipeViewCreator.Type.ENCRYPTED),
            triggers = TriggersDefinition(
                mobDropTrigger = MobDropTrigger(
                    EntityType.BLAZE
                )
            ),
            conditions = ConditionsDefinition(
                biomeCondition = listOf(
                    Biome.NETHER_WASTES,
                    Biome.CRIMSON_FOREST
                )
            )
        )
    )

}