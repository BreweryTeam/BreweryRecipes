package dev.jsinco.recipes

import dev.jsinco.recipes.commands.RecipesCommand
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.listeners.EventListener
import dev.jsinco.recipes.permissions.CommandPermission
import dev.jsinco.recipes.permissions.LuckPermsPermission
import dev.jsinco.recipes.permissions.PermissionManager
import dev.jsinco.recipes.permissions.PermissionSetter
import dev.jsinco.recipes.recipe.RecipeUtil
import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

// Idea:
// Allow recipes for brews to be collected from randomly generated chests and make some recipes rarer than others
// Has a gui that shows all the recipes the player has collected and how to make them
// Pulls directly from the Brewery plugin's config.yml file
class Recipes : JavaPlugin() {
    companion object {
        lateinit var instance: Recipes
        lateinit var permissionManager: PermissionManager
        lateinit var recipesConfig: RecipesConfig
    }

    override fun onEnable() {
        recipesConfig = readConfig()

        Bukkit.getScheduler().runTask(this) { ->
            permissionManager = when (recipesConfig.recipeSavingMethod) {
                PermissionSetter.LUCKPERMS -> LuckPermsPermission()
                PermissionSetter.COMMAND -> CommandPermission()
            }
        }

        Bukkit.getPluginManager().registerEvents(EventListener(this), this)
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, RecipesCommand::register)
        RecipeUtil.loadAllRecipes()
    }

    private fun readConfig(): RecipesConfig {
        return ConfigManager.create(RecipesConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer())
            it.withBindFile(File(this.dataFolder, "config.yml"))
            it.saveDefaults()
            it.load(true)
        }
    }

    override fun onLoad() {
        instance = this
    }

    fun reload() {
        recipesConfig = readConfig()
        RecipeUtil.loadAllRecipes()
    }
}