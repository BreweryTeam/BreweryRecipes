package dev.jsinco.recipes.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.util.BookUtil
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

object RecipesCommand {

    fun command(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("recipes")
            .then(
                Commands.literal("givebook")
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) return@executes 1
                        giveBook(sender)
                        1
                    }
                    .then(
                        Commands.argument("targets", ArgumentTypes.players())
                            .executes { context ->
                                val targets = context
                                    .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(context.source)
                                for (target in targets) {
                                    giveBook(target)
                                }
                                1
                            }
                    )
                    .requires { it.sender.hasPermission("recipes.command.givebook") }
            ).then(
                RecipeAddCommand.command()
                    .requires { it.sender.hasPermission("recipes.command.recipe.add") }
            ).then(
                Commands.literal("clear")
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            return@executes 1
                        }
                        Recipes.recipeViewManager.clearAll(sender.uniqueId)
                        1
                    }.requires { it.sender.hasPermission("recipes.command.recipe.clear") }
            ).build()
    }

    private fun giveBook(player: Player) {
        val item = BookUtil.createBook()
        if (!player.inventory.addItem(item).isEmpty()) {
            player.location.world.dropItemNaturally(player.location, item)
        }
    }
}