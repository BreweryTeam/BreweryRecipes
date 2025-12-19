package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.Recipes
import net.kyori.adventure.util.TriState
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File

class MigrationListener : Listener {

    private val permissionTemplate: String

    init {
        val configFile = File(Recipes.instance.dataFolder.parentFile, "BreweryX/addons/Recipes/recipesConfig.yml")
        permissionTemplate = if (configFile.exists()) {
            val config = YamlConfiguration.loadConfiguration(configFile)
            config.getString("recipe-permission-node")
                ?.takeIf { it.isNotBlank() }
                ?: "brewery.recipesaddon.recipe.%recipe%"
        } else {
            "brewery.recipesaddon.recipe.%recipe%"
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!Recipes.recipesConfig.migrate) return
        for ((recipeId, recipe) in Recipes.recipes()) {
            val permission = permissionTemplate.replace("%recipe%", recipeId)
            if (event.player.permissionValue(permission) != TriState.TRUE) continue
            Recipes.recipeViewManager.insertOrMergeView(
                event.player.uniqueId,
                recipe.generateCompletedView()
            )
        }
    }
}
