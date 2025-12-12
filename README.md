# LivesPlugin Repository Documentation

## Overview
LivesPlugin is a Minecraft Paper plugin (1.21.4) that implements a custom ability and weapon system. Players unlock powerful abilities for custom weapons by earning points from killing other players.

## Project Structure
- **Type**: Maven Project
- **Main Class**: `org.novagladecode.livesplugin.LivePlugin` (File: `LivePlugin.java`)
- **Configuration**: `Plugin.yml`
- **Data Storage**: `playerdata.yml` (Stores UUID, ability points)

### Ability Unlock System
Players earn **Ability Points** by eliminating other players. These points are temporary and reset on death.
- **Gain Points**: +1 Ability Point per kill.
- **Reset**: Ability Points reset to 0 upon death.
- **Usage**: Ability Points are required to use special abilities on custom Mace weapons.


### Custom Weapons
The plugin adds powerful custom weapons with unique abilities. Abilities often require Ability Points to use.

#### Warden Mace
*Ability 1 requires 3 Points. Ability 2 requires 6 Points.*
- **Recipe**:
  ```
  D H D
  D M D
  D D D
  ```
  (D = Diamond Block, H = Echo Shard, M = Heavy Core)

- **Abilities**:
    1.  **Sonic Wave** (`/wardenmace 1`): Releases a powerful radial shockwave that knocks back and damages enemies. (Cooldown: 4m)
    2.  **Warden's Grasp** (`/wardenmace 2`): Traps an enemy in Sculk jaws, damaging and pulling them. (Cooldown: 5m)

#### Nether Mace
*Ability 1 requires 3 Points. Ability 2 requires 6 Points.*
- **Recipe**:
  ```
  I S I
  R M R
  S S S
  ```
  (I = Netherite Ingot, S = Netherite Scrap, R = Blaze Rod, M = Heavy Core)

- **Abilities**:
    1.  **Infernal Wrath** (`/nethermace 1`): Summons a meteor shower of fireballs and falling blocks. (Cooldown: 4m)
    2.  **Fire Tornado** (`/nethermace 2`): Summons a fiery tornado around you and grants temporary flight. (Cooldown: 6m)

#### End Mace
*Ability 1 requires 3 Points. Ability 2 requires 6 Points.*
- **Recipe**:
  ```
  P E P
  E M E
  P B P
  ```
  (P = Popped Chorus Fruit, E = Ender Eye, M = Heavy Core, B = Breeze Rod)

- **Abilities**:
    1.  **Void Cloak** (`/endmace 1`): Vanish completely for 10 seconds. attacking reveals you. (Cooldown: 1m)
    2.  **Singularity** (`/endmace 2`): Creates a black hole that pulls enemies in, damages them, and launches them away. (Cooldown: 5m)

#### Chicken Bow
- **Recipe**:
  ```
  D F D
  F B F
  D F D
  ```
  (D = Diamond, F = Feather, B = Bow)
- **Passive Abilities**:
    - 50% chance to inflict Levitation/Slow Falling.
    - 40% chance to summon an angry chicken minion.

## Commands
- `/trust <player>`: Trust a player (prevents friendly fire from Mace abilities)
- `/untrust <player>`: Untrust a player
- `/wardenmace <1/2>`: Use Warden Mace abilities
- `/nethermace <1/2>`: Use Nether Mace abilities
- `/endmace <1/2>`: Use End Mace abilities
- `/weapon give <type>`: (OP) Get custom weapons

## Build and Release
This project uses GitHub Actions for automated builds and releases.
- **Trigger**: Creating a new Release on GitHub (e.g., tag `v1.2`).
- **Process**: 
    1. The workflow extracts the version number from the tag.
    2. Updates `pom.xml` version.
    3. Compiles the plugin (JDK 21).
    4. Uploads the JAR file to release assets.
