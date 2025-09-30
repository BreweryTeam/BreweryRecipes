package dev.jsinco.recipes

import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.configuration.RecipesTranslator
import dev.jsinco.recipes.gui.RecipeItem
import dev.jsinco.recipes.listeners.GuiEventListener
import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.function.Supplier

// Idea:
// Allow recipes for brews to be collected from randomly generated chests and make some recipes rarer than others
// Has a gui that shows all the recipes the player has collected and how to make them
// Pulls directly from the Brewery plugin's config.yml file
class Recipes : JavaPlugin() {
    companion object {
        lateinit var instance: Recipes
        lateinit var recipesConfig: RecipesConfig
        lateinit var recipesProvider: Supplier<List<RecipeItem>>

        fun key(key: String): NamespacedKey? {
            if (key.contains(":")) {
                return NamespacedKey.fromString(key)
            }
            return NamespacedKey("brewery_recipes", key)
        }
    }

    override fun onEnable() {
        recipesConfig = readConfig()

        GlobalTranslator.translator().addSource(RecipesTranslator(dataFolder))
        Bukkit.getPluginManager().registerEvents(GuiEventListener(this), this)
    }

    private fun readConfig(): RecipesConfig {
        return ConfigManager.create(RecipesConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer())
            it.withBindFile(File(this.dataFolder, "config.yml"))
            it.saveDefaults()
            it.load(true)
            it.save()
        }
    }

    override fun onLoad() {
        instance = this
    }

    fun reload() {
        recipesConfig = readConfig()
    }
}