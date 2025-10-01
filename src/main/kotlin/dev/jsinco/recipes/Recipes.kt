package dev.jsinco.recipes

import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.commands.RecipesCommand
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.configuration.RecipesTranslator
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.listeners.GuiEventListener
import dev.jsinco.recipes.util.TBPRecipeConverter
import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

// Idea:
// Allow recipes for brews to be collected from randomly generated chests and make some recipes rarer than others
// Has a gui that shows all the recipes the player has collected and how to make them
// Pulls directly from the Brewery plugin's config.yml file
class Recipes : JavaPlugin() {
    companion object {
        lateinit var instance: Recipes
        lateinit var recipesConfig: RecipesConfig
        private lateinit var recipeList: Map<String, BreweryRecipe>

        fun key(key: String): NamespacedKey? {
            if (key.contains(":")) {
                return NamespacedKey.fromString(key)
            }
            return NamespacedKey("brewery_recipes", key)
        }

        /**
         * TBP can have a very late initialization on some recipes, use only post start
         */
        fun recipes(): Map<String, BreweryRecipe> {
            if (this::recipeList.isInitialized && !recipeList.isEmpty()) {
                return recipeList
            }
            recipeList = instance.loadRecipeProvider()!!
                .asSequence()
                .map { it.identifier to it }
                .toMap()
            return recipeList
        }
    }

    override fun onEnable() {
        recipesConfig = readConfig()

        val translator = RecipesTranslator(File(dataFolder, "locale"))
        translator.reload()
        GlobalTranslator.translator().addSource(translator)
        Bukkit.getPluginManager().registerEvents(GuiEventListener(this), this)
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(RecipesCommand.command())
        }
    }

    private fun loadRecipeProvider(): List<BreweryRecipe>? {
        try {
            Class.forName("dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi")
            if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi::class.java)) {
                return null
            }
            val provider =
                (Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi::class.java)?.provider) ?: return null
            return provider.recipeRegistry.recipes
                .map { TBPRecipeConverter.convert(it) }
        } catch (ignored: NoClassDefFoundError) {
            return null
        }
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