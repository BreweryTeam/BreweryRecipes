package dev.jsinco.recipes.commands.subcommands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.jsinco.recipes.Util.getRecipeBookItem
import dev.jsinco.recipes.commands.arguments.RecipesArgumentType
import dev.jsinco.recipes.recipe.Recipe
import dev.jsinco.recipes.recipe.RecipeItem
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver

object GiveCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("give")
            .requires { it.sender.hasPermission("recipes.command.give") }
            .then(
                Commands.argument("player", ArgumentTypes.player())
                    .then(
                        Commands.literal("book")
                            .executes { command ->
                                val playerSelector =
                                    command.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                val player = playerSelector.resolve(command.source).first()
                                player.give(
                                    getRecipeBookItem()
                                )
                                1
                            }
                    )
                    .then(
                        Commands.literal("recipe")
                            .then(
                                Commands.argument("recipe", RecipesArgumentType())
                                    .executes { command ->
                                        val playerSelector =
                                            command.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                        val player = playerSelector.resolve(command.source).first()
                                        val recipe = command.getArgument("recipe", Recipe::class.java)
                                        player.give(
                                            RecipeItem(recipe).item
                                        )
                                        1
                                    }
                            )
                    )
            )

    }
}