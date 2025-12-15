package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.recipe.BreweryRecipe
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

object RecipeRemoveCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {

        fun applyToTargets(context: CommandContext<CommandSourceStack>, targets: Collection<Player>): Int {
            val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
            for (target in targets) {
                Recipes.recipeViewManager.removeView(
                    target.uniqueId,
                    recipe.identifier
                )
            }
            return 1
        }

        return Commands.literal("remove")
            .then(
                Commands.argument("recipe-key", RecipeArgumentType)
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) return@executes 1
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
                    )
            )
    }
}