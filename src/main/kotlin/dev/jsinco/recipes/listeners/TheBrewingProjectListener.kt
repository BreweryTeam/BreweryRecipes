package dev.jsinco.recipes.listeners

import dev.jsinco.brewery.api.meta.MetaDataType
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

data class TheBrewingProjectListener(val api: TheBrewingProjectApi) : Listener {

    companion object {
        val COMPLETED_KEY: Key = Key.key("recipes", "completed")
    }

    @EventHandler(ignoreCancelled = true)
    fun onBarrelExtract(event: BarrelExtractEvent) {
        val result = event.transactionSession.result?.itemStack
        result ?: return
        val brew = api.brewManager.fromItem(result).orElse(null) ?: return
        val recipeKey = (brew.closestRecipe(api.recipeRegistry).orElse(null) ?: return)
            .recipeName;
        if (recipeKey == brew.meta(COMPLETED_KEY, MetaDataType.STRING)) {
            return
        }
        val brewModified = brew.withMeta(COMPLETED_KEY, MetaDataType.STRING, recipeKey)
        // TODO add tracking
    }

    @EventHandler(ignoreCancelled = true)
    fun onDistilleryExtract(event: DistilleryExtractEvent) {
        val result = event.transactionSession.result?.itemStack
        result ?: return
        val brew = api.brewManager.fromItem(result).orElse(null) ?: return
        val recipeKey = (brew.closestRecipe(api.recipeRegistry).orElse(null) ?: return)
            .recipeName;
        if (recipeKey == brew.meta(COMPLETED_KEY, MetaDataType.STRING)) {
            return
        }
        val brewModified = brew.withMeta(COMPLETED_KEY, MetaDataType.STRING, recipeKey)
        // TODO add tracking
    }
}
