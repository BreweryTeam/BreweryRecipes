package dev.jsinco.recipes.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.util.BookUtil
import dev.jsinco.recipes.util.TranslationArgumentUtil
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object RecipesCommand {

    fun command(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("breweryrecipes")
            .then(
                Commands.literal("reload")
                    .executes { context ->
                        Recipes.instance.reload()
                        context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.reload"))
                        1
                    }
                    .requires { it.sender.hasPermission("breweryrecipes.command.reload") }
            )
            .then(
                Commands.literal("givebook")
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.invalid.sender"))
                            return@executes 1
                        }
                        giveBook(sender)
                        context.source.sender.sendMessage(
                            Component.translatable(
                                "breweryrecipes.command.givebook",
                                TranslationArgumentUtil.players(listOf(sender))
                            )
                        )
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
                                context.source.sender.sendMessage(
                                    Component.translatable(
                                        "breweryrecipes.command.givebook",
                                        TranslationArgumentUtil.players(targets)
                                    )
                                )
                                1
                            }.requires { it.sender.hasPermission("breweryrecipes.command.others") }
                    )
                    .requires { it.sender.hasPermission("breweryrecipes.command.givebook") }
            ).then(
                RecipeOpenCommand.command()
                    .requires { it.sender.hasPermission("breweryrecipes.command.open") }
            ).then(
                RecipeAddCommand.command()
                    .requires { it.sender.hasPermission("breweryrecipes.command.add") }
            ).then(
                RecipeRemoveCommand.command()
                    .requires { it.sender.hasPermission("breweryrecipes.command.remove") }
            ).then(
                RecipeGiveCommand.command()
                    .requires { it.sender.hasPermission("breweryrecipes.command.give") }
            ).then(
                Commands.literal("clear")
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.invalid.sender"))
                            return@executes 1
                        }
                        context.source.sender.sendMessage(
                            Component.translatable(
                                "breweryrecipes.command.clear",
                                TranslationArgumentUtil.players(listOf(sender))
                            )
                        )
                        Recipes.recipeViewManager.removeAll(sender.uniqueId)
                        Recipes.completedRecipeManager.removeAll(sender.uniqueId)
                        1
                    }
                    .then(
                        Commands.argument("targets", ArgumentTypes.players())
                            .executes { context ->
                                val targets = context
                                    .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(context.source)
                                context.source.sender.sendMessage(
                                    Component.translatable(
                                        "breweryrecipes.command.clear",
                                        TranslationArgumentUtil.players(targets)
                                    )
                                )
                                for (target in targets) {
                                    Recipes.recipeViewManager.removeAll(target.uniqueId)
                                    Recipes.completedRecipeManager.removeAll(target.uniqueId)
                                }
                                1
                            }.requires { it.sender.hasPermission("breweryrecipes.command.others") }
                    )
                    .requires { it.sender.hasPermission("breweryrecipes.command.clear") }
            ).build()
    }

    private fun giveBook(player: Player) {
        val item = BookUtil.createBook()
        if (!player.inventory.addItem(item).isEmpty()) {
            player.location.world.dropItemNaturally(player.location, item)
        }
    }
}