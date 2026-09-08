package dev.jsinco.recipes.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import dev.jsinco.recipes.util.Logger
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

// Injects brigadier commands into a running server
object SketchyCommandInjector {

    private const val PAPER_COMMANDS_CLASS = "io.papermc.paper.command.brigadier.PaperCommands"

    // https://github.com/PaperMC/Paper/blob/main/paper-server/src/main/java/io/papermc/paper/command/brigadier/PaperCommands.java
    fun inject(
        plugin: JavaPlugin,
        node: LiteralCommandNode<CommandSourceStack>,
        aliases: Collection<String> = emptyList()
    ) {
        try {
            val paperCommands = Class.forName(PAPER_COMMANDS_CLASS)
            val commands = paperCommands.getField("INSTANCE").get(null) as Commands
            val setValid = paperCommands.getMethod("setValid")
            val invalidate = paperCommands.getMethod("invalidate")
            setValid.invoke(commands)
            try {
                commands.register(plugin.pluginMeta, node, null, aliases)
            } finally {
                invalidate.invoke(commands)
            }
        } catch (e: Exception) {
            Logger.logErr("Failed to inject the brigadier command tree into the running server:")
            Logger.logErr(e)
            return
        }
        for (player in Bukkit.getOnlinePlayers()) {
            player.scheduler.run(plugin, { player.updateCommands() }, null)
        }
    }
}
