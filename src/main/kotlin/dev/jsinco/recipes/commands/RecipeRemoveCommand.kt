package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.util.TranslationArgumentUtil
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object RecipeRemoveCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {

        fun applyToTargets(context: CommandContext<CommandSourceStack>, targets: List<Player>): Int {
            val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
            for (target in targets) {
                Recipes.recipeViewManager.removeView(
                    target.uniqueId,
                    recipe.identifier
                )
            }
            context.source.sender.sendMessage(
                Component.translatable(
                    "recipes.command.remove",
                    TranslationArgumentUtil.players(targets),
                    TranslationArgumentUtil.recipe(recipe)
                )
            )
            return 1
        }

        return Commands.literal("remove")
            .then(
                Commands.argument("recipe-key", RecipeArgumentType)
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            context.source.sender.sendMessage(Component.translatable("recipes.command.invalid.sender"))
                            return@executes 1
                        }
                        applyToTargets(context, listOf(sender))
                    }
                    .then(
                        Commands.argument("targets", ArgumentTypes.players())
                            .executes { context ->
                                val targets = context
                                    .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(context.source)
                                applyToTargets(context, targets)
                            }
                    ).requires { it.sender.hasPermission("recipes.command.others") }
            )
    }
}