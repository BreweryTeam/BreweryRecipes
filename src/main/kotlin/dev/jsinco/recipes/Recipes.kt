package dev.jsinco.recipes

import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.commands.RecipesCommand
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.configuration.RecipesTranslator
import dev.jsinco.recipes.gui.RecipeItem
import dev.jsinco.recipes.gui.integration.TBPRecipe
import dev.jsinco.recipes.listeners.GuiEventListener
import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
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

        val translator = RecipesTranslator(File(dataFolder, "locale"))
        translator.reload()
        GlobalTranslator.translator().addSource(translator)
        Bukkit.getPluginManager().registerEvents(GuiEventListener(this), this)
        recipesProvider =
            loadRecipeProvider() ?: throw IllegalStateException("Needs either TBP or BreweryX to function")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(RecipesCommand.command())
        }
    }

    private fun loadRecipeProvider(): Supplier<List<RecipeItem>>? {
        try {
            Class.forName("dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi")
            if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi::class.java)) {
                return null
            }
            val provider =
                (Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi::class.java)?.provider) ?: return null
            return Supplier {
                provider.recipeRegistry.recipes
                    .map { TBPRecipe(it) }
            }
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