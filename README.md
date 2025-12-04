# LivesPlugin Repository Documentation

## Overview
LivesPlugin is a Minecraft Paper plugin (1.21.4) that implements a lives and leveling system. Players gain levels by killing others, which grants permanent potion effects. Losing all lives results in a ban, which can be reversed by other players using a special item.

## Project Structure
- **Type**: Maven Project
- **Main Class**: `org.novagladecode.livesplugin.LivePlugin` (File: `LivePlugin.java`)
- **Configuration**: `Plugin.yml`
- **Data Storage**: `playerdata.yml` (Stores UUID, lives, level, banned status)

## Features

### Lives System
- **Starting Lives**: 5
- **Death**: -1 Life
- **Zero Lives**: Player is banned.
- **Unbanning**: Requires another player to use the `/unban` command while holding an "Unban Item".

### Leveling System
- **Starting Level**: 5
- **Max Level**: 15
- **Gaining Levels**: Killing a player: Killer gains +1 Level (max 15)
- **Losing Levels**: Death: -1 Level (if level > 0)
- **Converting Levels to Items**: Use `/life withdraw` to convert 1 level into 1 Level Item (Nether Star)

### Effects
Effects change as you level up - debuffs are removed and buffs are added.

| Level | Debuffs Active | Buffs Active | Health |
| :--- | :--- | :--- | :--- |
| 1 | Mining Fatigue I, Slowness I, Hunger I, Glowing I | - | 10 hearts |
| 2 | Slowness I, Hunger I, Glowing I | - | 10 hearts |
| 3 | Hunger I, Glowing I | - | 10 hearts |
| 4 | Glowing I | - | 10 hearts |
| 5 | - | - | 10 hearts |
| 6 | - | Hero of the Village I | 10 hearts |
| 7 | - | Hero of the Village I, Luck I | 10 hearts |
| 8 | - | Hero, Luck, Speed I | 10 hearts |
| 9 | - | Hero, Luck, Speed + Invisibility (active ability) | 10 hearts |
| 10 | - | Hero, Luck, Speed, Invisibility (active) + Strength I | 10 hearts |
| 11 | - | Hero, Luck, Speed, Invisibility (active), Strength | **11 hearts** |
| 12 | - | Hero, Luck, Speed, Invisibility (active), Strength | **12 hearts** |
| 13 | - | Hero, Luck, Speed, Invisibility (active), Strength | **13 hearts** |
| 14 | - | Hero, Luck, Speed, Invisibility (active), Strength | **14 hearts** |
| 15 | - | Hero, Luck, Speed, Invisibility (active), Strength | **15 hearts** |



### Items
- **Level Item**:
    - Material: Nether Star
    - Name: `§6Level Item`
    - Usage: Created via `/life withdraw` command (costs 1 level). Right-click to gain 1 level (consumes item). Can also be used in crafting the Unban Token.

- **Unban Token**:
    - Material: Nether Star
    - Name: `§5Unban Token`
    - Usage: Right-click to open a GUI to select and revive a banned player. Revived players start at level 1.
    - Recipe: Crafted with 4 Netherite Scrap, 4 Diamond Blocks, and 1 Level Item.
      - Shape:
        ```
        D N D
        N L N
        D N D
        ```
        (N = Netherite Scrap, D = Diamond Block, L = Level Item)

## Commands
- `/life level`: Check your current level
- `/life withdraw`: Convert 1 level into a Level Item
- `/life reset`: Reset your level to 5 (OP only)
- `/life help`: Show all available commands
- `/invis`: Activate invisibility for 30 seconds (3 minute cooldown) (Requires Level 9+)


## Permissions
*No permissions are currently defined in `Plugin.yml` or checked in the code.*

## Build and Release
This project uses GitHub Actions for automated builds and releases.
- **Trigger**: Creating a new Release on GitHub (e.g., tag `v1.2`).
- **Process**: 
    1. The workflow extracts the version number from the tag (e.g., `1.2`).
    2. It updates the `pom.xml` version to match.
    3. It compiles the plugin (JDK 21). `Plugin.yml` automatically inherits the version.
    4. It uploads the resulting JAR file (e.g., `LivesPlugin-1.2.jar`) to the release assets.

