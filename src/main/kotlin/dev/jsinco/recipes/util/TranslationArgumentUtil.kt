package dev.jsinco.recipes.util

import dev.jsinco.recipes.recipe.BreweryRecipe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.entity.Player

object TranslationArgumentUtil {

    fun players(players: List<Player>): ComponentLike {
        return Argument.tagResolver(
            Placeholder.component(
                "player_names",
                players.stream()
                    .map(Player::displayName)
                    .collect(Component.toComponent(Component.text(", ")))
            )
        )
    }

    fun recipe(breweryRecipe: BreweryRecipe): ComponentLike {
        return Argument.tagResolver(
            Placeholder.unparsed("recipe_name", breweryRecipe.identifier)
        )
    }
}