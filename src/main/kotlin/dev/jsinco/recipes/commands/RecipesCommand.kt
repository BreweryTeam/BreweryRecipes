package dev.jsinco.recipes.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import dev.jsinco.recipes.util.BookUtil
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

object RecipesCommand {

    fun command(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("recipes")
            .then(
                Commands.literal("book")
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            return@executes 1
                        }
                        val item = BookUtil.createBook()
                        if (!sender.inventory.addItem(item).isEmpty()) {
                            sender.location.world.dropItemNaturally(sender.location, item)
                        }
                        return@executes 1
                    }
                    .requires { it.sender.hasPermission("recipes.command.book") }
            ).then(
                RecipeAddCommand.command()
            ).build()
    }
}