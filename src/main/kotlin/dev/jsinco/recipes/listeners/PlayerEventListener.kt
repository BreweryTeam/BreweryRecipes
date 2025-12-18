package dev.jsinco.recipes.listeners

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jsinco.recipes.recipe.RecipeViewManager
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.connection.PlayerLoginConnection
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEventListener(private val recipeViewManager: RecipeViewManager): Listener{

    @EventHandler
    fun onPlayerPlayerConnectionValidateLogin(event: PlayerConnectionValidateLoginEvent) {
        val profile: PlayerProfile? =
            when (val connection = event.getConnection()) {
                is PlayerLoginConnection -> {
                    connection.getAuthenticatedProfile()
                }

                is PlayerConfigurationConnection -> {
                    connection.getProfile()
                }

                else -> null
            }
        profile?.id?.let { playerUuid ->
            recipeViewManager.initiateViews(playerUuid)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        recipeViewManager.scheduleViewsUnload(event.player.uniqueId)
    }
}