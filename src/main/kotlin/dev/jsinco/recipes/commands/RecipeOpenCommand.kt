package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.jsinco.recipes.gui.GuiManager
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
                    context.source.sender.sendMessage(Component.translatable("recipes.command.invalid.sender"))
                    return@executes 1
                }
                GuiManager.openRecipeGui(sender)
                return@executes 1
            }
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
                    }.requires { it.sender.hasPermission("recipes.command.others") }
            )
    }

}
