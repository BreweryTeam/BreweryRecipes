package dev.jsinco.recipes.permissions

import com.dre.brewery.BreweryPlugin
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.configuration.RecipesConfig
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class CommandPermission : PermissionManager {

    override fun setPermission(permission: String, player: Player, value: Boolean) {
        BreweryPlugin.getScheduler().runTask {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(), Recipes.recipesConfig.permissionCommand
                    .replace("%player%", player.name).replace("%permission%", permission)
                    .replace("%boolean%", value.toString())
            )
        }
    }

    override fun removePermission(permission: String, player: Player) {
        BreweryPlugin.getScheduler().runTask {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(), Recipes.recipesConfig.permissionUnsetCommand
                    .replace("%player%", player.name).replace("%permission%", permission)
                    .replace("%boolean%", "false")
            )
        }
    }
}
