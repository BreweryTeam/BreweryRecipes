package dev.jsinco.recipes

import com.dre.brewery.recipe.BRecipe
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.recipes.commands.RecipesCommand
import dev.jsinco.recipes.configuration.GuiConfig
import dev.jsinco.recipes.configuration.RecipesConfig
import dev.jsinco.recipes.configuration.RecipesTranslator
import dev.jsinco.recipes.configuration.SpawnConfig
import dev.jsinco.recipes.configuration.serialize.*
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.RecipeViewManager
import dev.jsinco.recipes.data.DataManager
import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.gui.integration.BreweryXGuiInterface
import dev.jsinco.recipes.gui.integration.GuiIntegration
import dev.jsinco.recipes.gui.integration.TbpGuiInterface
import dev.jsinco.recipes.listeners.GuiEventListener
import dev.jsinco.recipes.listeners.RecipeListener
import dev.jsinco.recipes.listeners.RecipeSpawningListener
import dev.jsinco.recipes.util.BreweryXRecipeConverter
import dev.jsinco.recipes.util.ClassUtil
import dev.jsinco.recipes.util.TBPRecipeConverter
import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome
import org.bukkit.block.BlockType
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
        lateinit var guiConfig: GuiConfig
        lateinit var spawnConfig: SpawnConfig
        lateinit var recipeViewManager: RecipeViewManager
        private lateinit var recipeMap: Map<String, BreweryRecipe>

        fun key(key: String): NamespacedKey {
            if (key.contains(":")) {
                return NamespacedKey.fromString(key) ?: NamespacedKey("brewery_recipes", key)
            }
            return NamespacedKey("brewery_recipes", key)
        }

        /**
         * TBP can have a very late initialization on some recipes, use only post start
         */
        fun recipes(): Map<String, BreweryRecipe> {
            if (this::recipeMap.isInitialized && !recipeMap.isEmpty()) {
                return recipeMap
            }
            recipeMap = instance.loadRecipeProvider()!!
                .asSequence()
                .map { it.identifier to it }
                .toMap()
            return recipeMap
        }
    }

    lateinit var storageImpl: StorageImpl

    override fun onEnable() {
        recipesConfig = readConfig()
        guiConfig = readGuiConfig()
        spawnConfig = readSpawnConfig()
        storageImpl = DataManager(dataFolder).storageImpl
        recipeViewManager = RecipeViewManager(storageImpl)

        val translator = RecipesTranslator(File(dataFolder, "locale"), recipesConfig.language)
        translator.reload()
        GlobalTranslator.translator().addSource(translator)
        // TODO: Add BreweryX integration
        Bukkit.getPluginManager().registerEvents(GuiEventListener(this, TbpGuiInterface), this)
        Bukkit.getPluginManager().registerEvents(RecipeSpawningListener(), this)
        Bukkit.getPluginManager().registerEvents(RecipeListener(), this)
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(RecipesCommand.command())
        }
    }

    private fun loadRecipeProvider(): List<BreweryRecipe>? {
        if (ClassUtil.exists("dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi")) {
            if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi::class.java)) {
                return null
            }
            val provider =
                (Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi::class.java)?.provider) ?: return null
            return provider.recipeRegistry.recipes
                .map { TBPRecipeConverter.convert(it) }
        }
        if (ClassUtil.exists("com.dre.brewery.recipe.BRecipe")) {
            BRecipe.getRecipes()
                .map { BreweryXRecipeConverter.convert(it) }
        }
        throw IllegalStateException("Expected either BreweryX to be available or TBP")
    }

    private fun loadGuiIntegration(): GuiIntegration {
        if (ClassUtil.exists("dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi")) {
            return TbpGuiInterface
        }
        if (ClassUtil.exists("com.dre.brewery.recipe.BRecipe")) {
            return BreweryXGuiInterface
        }
        throw IllegalStateException("Expected either BreweryX to be available or TBP")
    }

    private fun readConfig(): RecipesConfig {
        return ConfigManager.create(RecipesConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer(), configSerializers().build())
            it.withBindFile(File(this.dataFolder, "config.yml"))
            it.saveDefaults()
            it.load(true)
            it.save()
        }
    }

    private fun readGuiConfig(): GuiConfig {
        return ConfigManager.create(GuiConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer(), configSerializers().build())
            it.withBindFile(File(this.dataFolder, "gui.yml"))
            it.saveDefaults()
            it.load(true)
            it.save()
        }
    }

    private fun configSerializers(): SerdesPackBuilder {
        return SerdesPackBuilder()
            .add(ComponentSerializer)
            .add(KeySerializer)
            .add(ConfigItemSerializer)
            .add(ConfigItemCollectionSerializer)
            .add(LoreSerializer)
            .add(LocaleSerializer)
            .add(ConditionsDefinitionSerializer)
            .add(TriggersDefinitionSerializer)
            .add(KeyedSerializer(RegistryKey.BLOCK, BlockType::class.java))
            .add(KeyedSerializer(RegistryKey.BIOME, Biome::class.java))
            .add(SpawnDefinitionSerializer)
    }

    private fun readSpawnConfig(): SpawnConfig {
        return ConfigManager.create(SpawnConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer(), configSerializers().build())
            it.withBindFile(File(this.dataFolder, "spawning.yml"))
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
        guiConfig = readGuiConfig()
        spawnConfig = readSpawnConfig()
    }
}