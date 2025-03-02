package dev.jsinco.recipes

import com.dre.brewery.BreweryPlugin
import com.dre.brewery.api.addons.AddonConfigManager
import com.dre.brewery.api.addons.AddonInfo
import com.dre.brewery.api.addons.BreweryAddon
import com.dre.brewery.utility.MinecraftVersion
import dev.jsinco.recipes.commands.AddonCommandManager
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.listeners.Events
import dev.jsinco.recipes.permissions.CommandPermission
import dev.jsinco.recipes.permissions.LuckPermsPermission
import dev.jsinco.recipes.permissions.PermissionManager
import dev.jsinco.recipes.permissions.PermissionSetter
import dev.jsinco.recipes.recipe.RecipeUtil

// Idea:
// Allow recipes for brews to be collected from randomly generated chests and make some recipes rarer than others
// Has a gui that shows all the recipes the player has collected and how to make them
// Pulls directly from the Brewery plugin's config.yml file
@AddonInfo(
    author = "Jsinco",
    version = "BX3.4.10",
    name = "Recipes",
    description = "A unique way to collect and view recipes for brews"
)
class Recipes : BreweryAddon() {

    companion object {
        lateinit var permissionManager: PermissionManager
            private set
        lateinit var addon: Recipes
            private set
        lateinit var configManager: AddonConfigManager
            private set
    }

    override fun onAddonPreEnable() {
        addon = this
        configManager = addonConfigManager
    }

    override fun onAddonEnable() {
        val mcV = BreweryPlugin.getMCVersion()
        if (mcV.isOrEarlier(MinecraftVersion.V1_13)) {
            addonLogger.info("This addon uses PersistentDataContainers (PDC) which were added in API version 1.14.1. This addon is not compatible with your server version: &7(" + mcV.version + ")")
        }

        val config = configManager.getConfig(RecipesConfig::class.java)

        BreweryPlugin.getScheduler().runTask {
            permissionManager = when (config.recipeSavingMethod) {
                PermissionSetter.LUCKPERMS -> LuckPermsPermission()
                PermissionSetter.COMMAND -> CommandPermission()
            }
        }

        registerListener(Events(breweryPlugin))
        registerCommand("recipes", AddonCommandManager(breweryPlugin))

        RecipeUtil.loadAllRecipes()
    }

    override fun onAddonDisable() {
        addonLogger.info("Recipes addon disabled.")
    }

    override fun onBreweryReload() {
        for (config in configManager.loadedConfigs) {
            config.reload()
        }
        RecipeUtil.loadAllRecipes()
    }
}