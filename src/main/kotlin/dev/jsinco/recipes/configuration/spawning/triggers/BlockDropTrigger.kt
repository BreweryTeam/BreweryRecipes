package dev.jsinco.recipes.configuration.spawning.triggers

import org.bukkit.block.BlockType

class BlockDropTrigger(
    vararg val blocks: BlockType,
) : SpawnTrigger