package dev.jsinco.recipes.commands.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.jsinco.recipes.recipe.Recipe
import dev.jsinco.recipes.recipe.RecipeUtil
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import java.util.concurrent.CompletableFuture

class RecipesArgumentType : CustomArgumentType.Converted<Recipe, String> {

    companion object {
        val INVALID_ARGUMENT = DynamicCommandExceptionType {
            MessageComponentSerializer.message().serialize(Component.text("Invalid argument '$it'"))
        }
    }

    override fun convert(nativeType: String): Recipe {
        val recipe = RecipeUtil.getRecipeFromKey(nativeType)
        return recipe ?: throw INVALID_ARGUMENT.create(nativeType)
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        RecipeUtil.getAllRecipes()
            .stream()
            .map(Recipe::recipeKey)
            .filter { it.startsWith(builder.remaining) }
            .forEach { builder.suggest(it) }
        return builder.buildFuture()
    }
}