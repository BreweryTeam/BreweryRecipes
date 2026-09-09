package dev.jsinco.recipes.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jsinco.recipes.commands.argument.EnumArgument
import dev.jsinco.recipes.commands.argument.RecipeArgumentType
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.util.RecipeItemUtil
import dev.jsinco.recipes.util.TranslationArgumentUtil
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

object RecipeGiveCommand {

    fun command(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("give")
            .then(
                Commands.literal("scribbling")
                    .then(
                        Commands.argument("recipe-key", RecipeArgumentType)
                            .then(
                                Commands.argument("flaw-type", EnumArgument.flawType())
                                    .withTargets { context, targets -> giveScribbling(context, targets, true) }
                            )
                            .withTargets { context, targets -> giveScribbling(context, targets, false) }
                    )
            )
            .then(
                Commands.literal("recipe")
                    .then(
                        Commands.argument("recipe-key", RecipeArgumentType)
                            .then(
                                Commands.argument("flaw-type", EnumArgument.flawType())
                                    .then(
                                        Commands.argument(
                                            "flaw-level",
                                            DoubleArgumentType.doubleArg(0.0, 100.0)
                                        ).withTargets { context, targets -> giveRecipeItem(context, targets) }
                                    )
                                    .withTargets { context, targets -> giveRecipeItem(context, targets) }
                            )
                            .withTargets { context, targets -> giveRecipeItem(context, targets) }
                    )
            )
    }

    private fun <T : ArgumentBuilder<CommandSourceStack, T>> T.withTargets(
        action: (CommandContext<CommandSourceStack>, List<Player>) -> Int
    ): T {
        return this.executes { context ->
            val sender = context.source.sender
            if (sender !is Player) {
                context.source.sender.sendMessage(Component.translatable("breweryrecipes.command.invalid.sender"))
                return@executes 1
            }
            action(context, listOf(sender))
        }.then(
            Commands.argument("targets", ArgumentTypes.players())
                .executes { context ->
                    val targets = context
                        .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                        .resolve(context.source)
                    action(context, targets)
                }.requires { it.sender.hasPermission("breweryrecipes.command.others") }
        )
    }

    private fun giveScribbling(
        context: CommandContext<CommandSourceStack>,
        targets: List<Player>,
        specificFlawType: Boolean
    ): Int {
        val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
        val base = ItemType.PAPER.createItemStack()
        val item = if (specificFlawType) {
            recipe.lootItem(base, context.getArgument("flaw-type", RecipeViewCreator.Type::class.java))
        } else {
            recipe.lootItem(base)
        }
        return give(context, targets, recipe, item)
    }

    private fun giveRecipeItem(context: CommandContext<CommandSourceStack>, targets: List<Player>): Int {
        val recipe = context.getArgument("recipe-key", BreweryRecipe::class.java)
        val flawType = context.optionalArgument("flaw-type", RecipeViewCreator.Type::class.java)
        val flawLevel = context.optionalArgument("flaw-level", Double::class.javaObjectType)
        val item = RecipeItemUtil.createRecipeItem(RecipeItemUtil.rollView(recipe, flawType, flawLevel)) ?: run {
            context.source.sender.sendMessage(
                Component.translatable("breweryrecipes.command.invalid.recipe-item")
                    .color(NamedTextColor.RED)
            )
            return 1
        }
        return give(context, targets, recipe, item)
    }

    private fun give(
        context: CommandContext<CommandSourceStack>,
        targets: List<Player>,
        recipe: BreweryRecipe,
        item: ItemStack
    ): Int {
        if (targets.isEmpty()) {
            context.source.sender.sendMessage(
                Component.translatable("argument.entity.notfound.player")
                    .color(NamedTextColor.RED)
            )
            return 1
        }
        for (target in targets) {
            target.inventory.addItem(item.clone())
                .values
                .forEach { leftover -> target.location.world.dropItemNaturally(target.location, leftover) }
        }
        context.source.sender.sendMessage(
            Component.translatable(
                "breweryrecipes.command.give",
                TranslationArgumentUtil.players(targets),
                TranslationArgumentUtil.recipe(recipe)
            )
        )
        return 1
    }

    private fun <T : Any> CommandContext<CommandSourceStack>.optionalArgument(name: String, clazz: Class<T>): T? {
        return try {
            this.getArgument(name, clazz)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
