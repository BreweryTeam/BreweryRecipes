package dev.jsinco.recipes.commands.subcommands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.jsinco.recipes.guis.RecipeGui
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver

object BookCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("book")
            .then(Commands.argument("player", ArgumentTypes.player()).executes { commands ->
                val player = commands.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                    .resolve(commands.source).first()
                RecipeGui(player).openRecipeGui(player)
                1
            })
            .requires {it.sender.hasPermission("recipes.command.book")}

    }
}