package dev.jsinco.recipes.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.commands.argument.EnumArgument
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

object RecipeAddCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {

        fun applyToTargets(context: CommandContext<CommandSourceStack>, targets: Collection<Player>): Int {
            val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
            val flawLevel = context.getArgument("flaw-level", Double::class.java)
            val flawType = context.getArgument("flaw-type", RecipeViewCreator.Type::class.java)
            for (target in targets) {
                Recipes.recipeViewManager.insertOrMergeView(
                    target.uniqueId,
                    recipe.generate(flawLevel, flawType)
                )
            }
            return 1
        }

        return Commands.literal("add")
            .then(
                Commands.argument("recipe-key", RecipeArgumentType)
                    .then(
                        Commands.argument("flaw-type", EnumArgument(RecipeViewCreator.Type::class.java))
                            .then(
                                Commands.argument("flaw-level", DoubleArgumentType.doubleArg(0.0, 100.0))
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
                    )
            )
    }
}