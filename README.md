# BreweryRecipes (standalone)
A tool to help your users discover brewing recipes for either BreweryX or TheBrewingProject. Recipes can be found in loot, such as chests, fishing, mob kloot, and block drops.

## Here's an example of how the gui could look like
<img width="510" height="369" alt="Screenshot_20251112_110941" src="https://github.com/user-attachments/assets/b158ec0e-f6dd-4115-b037-f24410cbcca0" />

Where a recipe can be found in either complete or incomplete form, **an example of an incomplete recipe could look like this:**
<img width="812" height="137" alt="image" src="https://github.com/user-attachments/assets/0af3ce0c-8986-47eb-a38c-3fc36e0969c8" />

## Installation

Download the plugin from either [hangar](https://hangar.papermc.io) or [modrinth](https://modrinth.com/) and then put the plugin into the `./plugins` folder.

> [!CAUTION]
> This is no longer a BreweryX only addon. Just put it into the plugins folder like any other plugin

> [!IMPORTANT]
> This plugin needs either TheBrewingProject or BreweryX to function

## Gameplay
Craft a book with one `minecraft:book` and one `minecraft:paper`. Find recipe fragments in chests, when fishing, when killing mobs, or when breaking blocks.

## Permissions
- `recipes.command` root node for all commands in the plugin
- `recipes.command.recipe.add` node for the recipes add command
- `recipes.command.recipe.clear` node for the recipes clear command
- `recipes.command.book` node for the book command
- `recipes.override.view` override node for showing all recipes in the book

## Configuration
You can configure how recipes spawn (`spawning.yml`), how the gui will look like (`gui.yml`), and basic things like language and storage (`config.yml`). See the respective configuration files for more information about it, it's clearly documented
