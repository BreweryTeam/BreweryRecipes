package dev.jsinco.recipes.commands.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import java.util.*
import java.util.concurrent.CompletableFuture

class EnumArgument<E : Enum<E>>(val eClass: Class<E>) : CustomArgumentType.Converted<E, String> {

    val UNKNOWN_VALUE = DynamicCommandExceptionType {
        return@DynamicCommandExceptionType MessageComponentSerializer.message()
            .serialize(
                TranslationUtil.render(
                    Component.translatable(
                        "recipes.command.invalid.argument",
                        Argument.numeric("argument", it.toString())
                    )
                )
            )
    }

    @Throws(CommandSyntaxException::class)
    override fun convert(nativeType: String): E {
        return eClass.enumConstants.first { nativeType.equals(it.name, true) }
            ?: throw UNKNOWN_VALUE.create(nativeType)
    }


    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        eClass.enumConstants
            .map { it.name }
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.startsWith(builder.remainingLowerCase) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word();
    }
}
