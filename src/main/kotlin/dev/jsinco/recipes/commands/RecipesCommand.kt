package dev.jsinco.recipes.commands

import dev.jsinco.recipes.commands.subcommands.BookCommand
import dev.jsinco.recipes.commands.subcommands.GiveCommand
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent

object RecipesCommand {

    fun register(commandsReloadableRegistrarEvent: ReloadableRegistrarEvent<Commands>) {
        commandsReloadableRegistrarEvent.registrar().register(
            Commands.literal("breweryrecipes")
                .then(GiveCommand.command())
                .then(BookCommand.command())
                .build(),
            setOf("recipes", "brecipes")
        )
    }
}