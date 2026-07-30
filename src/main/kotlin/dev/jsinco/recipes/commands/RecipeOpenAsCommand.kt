package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.jsinco.recipes.commands.argument.JoinedPlayerArgumentType
import dev.jsinco.recipes.gui.GuiManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object RecipeOpenAsCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("openas")
            .then(
                Commands.argument("target", JoinedPlayerArgumentType)
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.invalid.sender"))
                            return@executes 1
                        }
                        val target = context.getArgument("target", OfflinePlayer::class.java)
                        GuiManager.openRecipeGui(sender, target)
                        return@executes 1
                    }
            )
    }

}
