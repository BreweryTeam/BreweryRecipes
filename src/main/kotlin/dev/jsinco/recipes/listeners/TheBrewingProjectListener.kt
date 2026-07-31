package dev.jsinco.recipes.listeners

import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.meta.MetaDataType
import dev.jsinco.brewery.api.recipe.Recipe
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent
import dev.jsinco.brewery.bukkit.api.event.transaction.ItemTransactionEvent
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource
import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.util.TBPRecipeConverter
import dev.jsinco.recipes.util.metadata.UuidMetaDataType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import kotlin.math.pow

data class TheBrewingProjectListener(val api: TheBrewingProjectApi) : Listener {

    companion object {
        val COMPLETED_RECIPE_KEY: Key = Key.key("recipes", "completed_recipe")
        val COMPLETED_SCORE_KEY: Key = Key.key("recipes", "completed_score")
        val COMPLETED_BY_KEY: Key = Key.key("recipes", "completed_by")
    }

    @EventHandler(ignoreCancelled = true)
    fun onBarrelExtract(event: BarrelExtractEvent) {
        onInventoryExtract(event, event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onDistilleryExtract(event: DistilleryExtractEvent) {
        onInventoryExtract(event, event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCauldronExtract(event: CauldronExtractEvent) {
        val prevBrew = api.brewManager.fromItem(event.itemResult.get()).orElse(null) ?: return
        val brew = actOnBrew(prevBrew, event.player ?: return) ?: return
        event.setResult(brew)
    }

    private fun actOnBrew(brew: Brew, player: Player): Brew? {
        val recipe = brew.closestRecipe(api.recipeRegistry).orElse(null) ?: return null
        val score = brew.score(recipe)
        if (!score.completed() || score.brewQuality() == null) {
            return null
        }
        val scoreValue = score.score()
        val recipeKey = recipe.recipeName
        if (!appliesTo(player, brew, scoreValue, recipeKey)) {
            return null
        }
        val brewModified = brew.withMeta(COMPLETED_RECIPE_KEY, MetaDataType.STRING, recipeKey)
            .withMeta(COMPLETED_BY_KEY, UuidMetaDataType, player.uniqueId)
            .withMeta(COMPLETED_SCORE_KEY, MetaDataType.DOUBLE, scoreValue)
        val existing = BreweryRecipes.completedRecipeManager.insertOrUpdateRecipeCompletion(
            player.uniqueId,
            TBPRecipeConverter.convert(recipe.recipeName, brew.completedSteps, score = scoreValue)
        )

        if (existing == null && BreweryRecipes.recipesConfig.showRecipeCompleteMessage) {
            completeRecipeFeedback(player, recipe.recipeName)
        }
        if (existing != null && existing.score < 1.0 && scoreValue >= 1.0 && BreweryRecipes.recipesConfig.showRecipePerfectMessage) {
            perfectRecipeFeedback(player, recipe.recipeName)
        }
        if (BreweryRecipes.recipesConfig.incrementalLearning) {
            learn(player, brew, recipe)
        }

        return brewModified
    }

    private fun onInventoryExtract(event: ItemTransactionEvent<ItemSource.ItemBasedSource>, player: Player?) {
        player ?: return
        val result = event.transactionSession.result?.itemStack ?: return
        val brew = api.brewManager.fromItem(result).orElse(null) ?: return
        val brewModified = actOnBrew(brew, player) ?: return
        event.transactionSession.result = ItemSource
            .ItemBasedSource(api.brewManager.toItem(brewModified, Brew.State.Other()))
    }

    private fun appliesTo(player: Player, brew: Brew, score: Double, recipeKey: String): Boolean {
        if ((brew.meta(COMPLETED_BY_KEY, UuidMetaDataType)?.let { player.uniqueId != it }) ?: false) {
            return false
        }
        return recipeKey != brew.meta(COMPLETED_RECIPE_KEY, MetaDataType.STRING) ||
                (brew.meta(COMPLETED_SCORE_KEY, MetaDataType.DOUBLE) ?: Double.MIN_VALUE) < score
    }

    private fun completeRecipeFeedback(player: Player, recipeIdentifier: String) {
        recipeFeedback(player, recipeIdentifier, "breweryrecipes.learn.new")
    }

    private fun perfectRecipeFeedback(player: Player, recipeIdentifier: String) {
        recipeFeedback(player, recipeIdentifier, "breweryrecipes.learn.perfect")
    }

    private fun recipeFeedback(player: Player, recipeIdentifier: String, translationKey: String) {
        val displayName = BreweryRecipes.brewingIntegration.brewDisplayName(recipeIdentifier) ?: return
        player.sendMessage(
            Component.translatable(
                translationKey,
                Argument.component("name", displayName)
            )
        )
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f)
    }

    private fun learn(player: Player, brew: Brew, recipe: Recipe<ItemStack>) {
        val breweryRecipe = BreweryRecipes.brewingIntegration.getRecipe(recipe.recipeName) ?: return
        val currentView = BreweryRecipes.recipeViewManager.getView(player.uniqueId, recipe.recipeName)
            ?: breweryRecipe.generateFullyFlawedView()

        val score = brew.score(recipe)
        if (!score.completed()) return
        val targetFragmentation = computeTargetFragmentation(score.score())

        val updatedView = RecipeViewLoreWriter.defragmentUntil(currentView, targetFragmentation)
        BreweryRecipes.recipeViewManager.insertOrUpdateView(player.uniqueId, updatedView)
    }

    private fun computeTargetFragmentation(score: Double): Double {
        val numHalfStars = (score * 10.0).toInt().toDouble()
        val learningCurve = BreweryRecipes.recipesConfig.learningCurve.coerceAtLeast(0.0)
        val startingFragmentation = BreweryRecipes.recipesConfig.startingFragmentation.coerceIn(0.0, 100.0)
        return startingFragmentation * (1.0 - (numHalfStars / 10.0).pow(learningCurve))
    }

}
