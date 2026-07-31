package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.gui.GuiManager
import dev.jsinco.recipes.gui.RecipeBookMode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object RecipeOpenCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("open")
            .executes { context ->
                val sender = context.source.sender
                if (sender !is Player) {
                    context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.invalid.sender"))
                    return@executes 1
                }
                GuiManager.openRecipeGui(sender)
                return@executes 1
            }
            .then(
                Commands.literal("admin")
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.invalid.sender"))
                            return@executes 1
                        }
                        val defaultMode = BreweryRecipes.guiConfig.defaultMode
                        val mode = if (defaultMode.hasOverridePermission(sender)) {
                            defaultMode
                        } else {
                            // player must have permission for other mode since requires() checked if player has either permission
                            defaultMode.next()
                        }
                        GuiManager.openWithMode(sender, mode, true)
                        return@executes 1
                    }.requires { stack ->
                        val sender = stack.sender
                        return@requires sender is Player && RecipeBookMode.entries.any { it.hasOverridePermission(sender) }
                    }
            )
            .then(
                Commands.argument("targets", ArgumentTypes.players())
                    .executes { context ->
                        val targets = context
                            .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                            .resolve(context.source)
                        targets.forEach { target ->
                            GuiManager.openRecipeGui(target)
                        }
                        return@executes 1
                    }.requires { it.sender.hasPermission("breweryrecipes.command.others") }
            )
    }

}
