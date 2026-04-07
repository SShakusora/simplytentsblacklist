# Simply Tents Blacklist

**A simple blacklist addon for Simply Tents mod**

---

## Overview

Simply Tents Blacklist is a lightweight addon mod for [Simply Tents](https://www.curseforge.com/minecraft/mc-mods/simply-tents) that allows server administrators and players to configure which blocks, items, and entities should **not** be picked up by tents. This provides fine-grained control over tent behavior, preventing valuable or restricted content from being moved.

---

## Features

- **Block Blacklist** - Prevent specific blocks from being picked up by tents (supports block registry names)
- **Item Blacklist** - Prevent entities or block entities containing specific items from being picked up
- **Tag Support** - Use block tags and item tags for flexible, category-based blacklisting
- **Recursive Item Detection** - Deep scans entity and block entity NBT data to detect blacklisted items
- **Server-Side Config** - All settings are configurable via server config file

---

## Configuration

The mod generates a config file at `config/simplytentsblacklist-common.toml`:

```toml
[blacklist]
	# List of block registry names that should not be picked up by tents.
	# Format: modid:block_name (e.g., "minecraft:chest", "minecraft:diamond_block")
	blacklistBlocks = []
	
	# List of item registry names that should prevent entities/block entities from being picked up.
	# If an entity or block entity contains any of these items, it will not be picked up.
	# Format: modid:item_name (e.g., "minecraft:diamond", "minecraft:emerald")
	blacklistItems = []
	
	# List of block tag names that should not be picked up by tents.
	# Format: #modid:tag_name (e.g., "#minecraft:logs", "#forge:ores")
	blacklistBlockTags = []
	
	# List of item tag names that should prevent entities/block entities from being picked up.
	# If an entity or block entity contains any items with these tags, it will not be picked up.
	# Format: #modid:tag_name (e.g., "#minecraft:logs", "#forge:gems")
	blacklistItemTags = []
```

### Example Configuration

```toml
[blacklist]
	blacklistBlocks = ["minecraft:chest", "minecraft:ender_chest", "minecraft:spawner"]
	blacklistItems = ["minecraft:diamond", "minecraft:netherite_ingot"]
	blacklistBlockTags = ["#minecraft:doors", "#forge:ores"]
	blacklistItemTags = ["#forge:gems", "#forge:ingots/gold"]
```

---

## How It Works

When a player attempts to pick up an area with a tent:

1. **Block Check** - If the block is in the block blacklist or matches a blacklisted block tag, it remains in the world
2. **Item Check** - If an entity or block entity contains any blacklisted items (or items matching blacklisted tags), it remains in the world
3. **Safe Pickup** - Only non-blacklisted blocks and entities are packed into the tent

---

## Installation

1. Install **Minecraft Forge** for Minecraft 1.20.1
2. Download and install **GeckoLib** (required dependency)
3. Download and install **Simply Tents**
4. Place this mod's `.jar` file into your `mods` folder
5. Launch the game and configure the blacklist in `config/simplytentsblacklist-common.toml`

---

## Compatibility

- **Server-Side**: This mod is required on the server to function properly
- **Client-Side**: Optional on client (for singleplayer worlds, install on both sides)

---

## License

This mod is licensed under the [MIT License](https://opensource.org/licenses/MIT).

---

## Credits

- **Author**: Shinonome Shakusora
- **Thanks**: The Simply Tents team for creating the original mod
