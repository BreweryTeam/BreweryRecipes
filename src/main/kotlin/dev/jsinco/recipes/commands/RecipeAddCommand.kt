package dev.jsinco.recipes.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.core.BreweryRecipe
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

object RecipeAddCommand {


    fun command(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("add")
            .then(
                Commands.argument("recipe-key", RecipeArgumentType)
                    .then(
                        Commands.argument("flaw-level", DoubleArgumentType.doubleArg(0.0, 100.0))
                            .executes { context ->
                                val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
                                val flawLevel = context.getArgument("flaw-level", Double::class.java)
                                val sender = context.source.sender as Player
                                Recipes.recipeViewManager.insertOrUpdateView(
                                    sender.uniqueId,
                                    recipe.generate(flawLevel)
                                )
                                1
                            }
                    )
            )
    }
}