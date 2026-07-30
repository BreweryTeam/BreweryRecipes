package dev.jsinco.recipes.commands.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.concurrent.CompletableFuture

object JoinedPlayerArgumentType : CustomArgumentType.Converted<OfflinePlayer, String> {

    val UNKNOWN_PLAYER = DynamicCommandExceptionType {
        return@DynamicCommandExceptionType MessageComponentSerializer.message()
            .serialize(
                TranslationUtil.render(
                    Component.translatable(
                        "breweryrecipes.command.invalid.argument",
                        Argument.string("argument", it.toString())
                    )
                )
            )
    }

    override fun convert(nativeType: String): OfflinePlayer {
        val offlinePlayer = try {
            Bukkit.getOfflinePlayer(UUID.fromString(nativeType))
        } catch (_: IllegalArgumentException) {
            Bukkit.getOfflinePlayerIfCached(nativeType)
        }
        if (offlinePlayer?.hasPlayedBefore() == true) {
            return offlinePlayer
        }
        throw UNKNOWN_PLAYER.create(nativeType)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        Bukkit.getOnlinePlayers()
            .map { it.name }
            .forEach { builder.suggest(it) }
        return builder.buildFuture()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

}
