package dev.jsinco.recipes.commands.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import java.util.concurrent.CompletableFuture

object RecipeArgumentType : CustomArgumentType.Converted<BreweryRecipe, String> {

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
    val WORD_ARGUMENT: Regex = "[a-zA-Z0-9+\\-_.]+".toRegex()


    override fun convert(nativeType: String): BreweryRecipe {
        return Recipes.recipes().get(nativeType) ?: throw UNKNOWN_VALUE.create(nativeType)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        Recipes.recipes().values.asSequence()
            .map { it.identifier }
            .filter { recipeName -> recipeName.startsWith(builder.getRemainingLowerCase()) }
            .map(this::sanitizeName)
            .forEach(builder::suggest);
        return builder.buildFuture();
    }


    private fun sanitizeName(name: String): String {
        if (WORD_ARGUMENT.matches(name)) {
            return name
        }
        return "\"" + name + "\""
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }
}