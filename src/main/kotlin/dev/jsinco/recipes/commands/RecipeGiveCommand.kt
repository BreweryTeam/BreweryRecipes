package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jsinco.recipes.commands.argument.EnumArgument
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.util.TranslationArgumentUtil
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object RecipeGiveCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {

        fun applyToTargets(
            context: CommandContext<CommandSourceStack>,
            targets: List<Player>,
            specificFlawType: Boolean
        ): Int {
            val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
            var item = recipe.lootItem()
            if (specificFlawType) {
                item = recipe.lootItem(context.getArgument("flaw-type", RecipeViewCreator.Type::class.java))
            }
            for (target in targets) {
                if (!target.inventory.addItem(item).isEmpty()) {
                    target.location.world.dropItemNaturally(target.location, item)
                }
            }
            context.source.sender.sendMessage(
                Component.translatable(
                    "recipes.command.give",
                    TranslationArgumentUtil.players(targets),
                    TranslationArgumentUtil.recipe(recipe)
                )
            )
            return 1
        }

        return Commands.literal("give")
            .then(
                Commands.argument("recipe-key", RecipeArgumentType)
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) {
                            context.source.sender.sendMessage(Component.translatable("recipes.command.invalid.sender"))
                            return@executes 1
                        }
                        applyToTargets(context, listOf(sender), false)
                    }
                    .then(
                        Commands.argument("flaw-type", EnumArgument(RecipeViewCreator.Type::class.java))
                            .executes { context ->
                                val targets = context
                                    .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(context.source)
                                applyToTargets(context, targets, true)
                            }
                            .then(
                                Commands.argument("targets", ArgumentTypes.players())
                                    .executes { context ->
                                        val targets = context
                                            .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(context.source)
                                        applyToTargets(context, targets, false)
                                    }
                            ).requires { it.sender.hasPermission("recipes.command.others") }
                    )
            )
    }
}