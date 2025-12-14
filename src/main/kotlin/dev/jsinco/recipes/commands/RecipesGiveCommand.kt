package dev.jsinco.recipes.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jsinco.recipes.commands.argument.EnumArgument
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

object RecipesGiveCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {

        fun applyToTargets(context: CommandContext<CommandSourceStack>, targets: Collection<Player>, specificFlawType: Boolean): Int {
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
            return 1
        }

        return Commands.literal("give")
            .then(
                Commands.argument("recipe-key", RecipeArgumentType)
                    .executes { context ->
                        val sender = context.source.sender
                        if (sender !is Player) return@executes 1
                        applyToTargets(context, listOf(sender), false)
                    }
                    .then(
                        Commands.argument("targets", ArgumentTypes.players())
                            .executes { context ->
                                val targets = context
                                    .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(context.source)
                                applyToTargets(context, targets, false)
                            }
                            .then(
                                Commands.argument("flaw-type", EnumArgument(RecipeViewCreator.Type::class.java))
                                    .executes { context ->
                                        val sender = context.source.sender
                                        if (sender !is Player) return@executes 1
                                        applyToTargets(context, listOf(sender), true)
                                    }

                            )
                    )
            )
    }
}