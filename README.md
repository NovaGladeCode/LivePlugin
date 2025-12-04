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
- **Starting Level**: 1
- **Gaining Levels**: Killing a player: Killer gains +1 Level directly (no item)
- **Losing Levels**: Death: -1 Level (if level > 0)
- **Converting Levels to Lives**: Use `/life withdraw` to convert 1 level into 1 life

### Effects
Effects change as you level up - debuffs are removed and buffs are added.

| Level | Debuffs Active | Buffs Active |
| :--- | :--- | :--- |
| 1 | Mining Fatigue I, Slowness I, Hunger I, Glowing I | - |
| 2 | Slowness I, Hunger I, Glowing I | - |
| 3 | Hunger I, Glowing I | - |
| 4 | Glowing I | - |
| 5 | - | - |
| 6 | - | Hero of the Village I |
| 7 | - | Hero of the Village I, Luck I |
| 8 | - | Hero, Luck, Speed I |
| 9 | - | Hero, Luck, Speed + Invisibility (toggle) |
| 10 | - | Hero, Luck, Speed, Invisibility + Strength I |



### Items
- **Unban Item**:
    - Material: Nether Star
    - Name: `§5Unban Item`
    - Usage: Required for `/unban` command.
    - Source: Crafted with 4 Diamonds and 1 Nether Star.
      - Shape:
        ```
          D
        D S D
          D
        ```
        (D = Diamond, S = Nether Star)

## Commands
- `/level`: Displays current level and remaining lives.
- `/invis`: Toggles invisibility effect (Requires Level 9+).
- `/life withdraw`: Converts 1 level into 1 life.
- `/unban <player>`: Unbans a player. Requires holding an "Unban Item".

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

