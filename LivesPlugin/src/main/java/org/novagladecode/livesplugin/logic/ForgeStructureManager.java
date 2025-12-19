package org.novagladecode.livesplugin.logic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.novagladecode.livesplugin.LivePlugin;

public class ForgeStructureManager {

    private final LivePlugin plugin;

    public ForgeStructureManager(LivePlugin plugin) {
        this.plugin = plugin;
    }

    public void buildForge(Location center, String type) {
        type = type.toLowerCase();

        // Clear area 5x5x3
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 3; y++) {
                    center.clone().add(x, y, z).getBlock().setType(Material.AIR);
                }
            }
        }

        // Base 5x5
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location loc = center.clone().add(x, -1, z);
                applyBaseBlock(loc, type, x, z);
            }
        }

        // Decorations and Pillars
        applyDecorations(center, type);

        // The Forge itself
        Block forgeBlock = center.getBlock();
        forgeBlock.setType(Material.CRAFTING_TABLE);
        forgeBlock.setMetadata("sacred_forge", new FixedMetadataValue(plugin, true));
        forgeBlock.setMetadata("forge_type", new FixedMetadataValue(plugin, type));

        // Register it persistently
        plugin.getForgeDataManager().addForge(center);
    }

    private void applyBaseBlock(Location loc, String type, int relX, int relZ) {
        Material mat = Material.STONE;
        boolean isCorner = (Math.abs(relX) == 2 && Math.abs(relZ) == 2);
        boolean isEdge = (Math.abs(relX) == 2 || Math.abs(relZ) == 2);

        switch (type) {
            case "warden":
                mat = isCorner ? Material.SCULK_CATALYST : (isEdge ? Material.SCULK : Material.SCULK);
                if (!isCorner && !isEdge && (relX != 0 || relZ != 0))
                    mat = Material.SCULK_CATALYST;
                break;
            case "nether":
                mat = isCorner ? Material.CRYING_OBSIDIAN : (isEdge ? Material.NETHERITE_BLOCK : Material.MAGMA_BLOCK);
                break;
            case "end":
                mat = isCorner ? Material.OBSIDIAN : (isEdge ? Material.END_STONE_BRICKS : Material.PURPUR_BLOCK);
                break;
            case "ghostblade":
                mat = isCorner ? Material.SOUL_SOIL : Material.SOUL_SAND;
                break;
            case "dragonblade":
                mat = isCorner ? Material.OBSIDIAN : (isEdge ? Material.GILDED_BLACKSTONE : Material.REDSTONE_BLOCK);
                break;
            case "mistblade":
                mat = isCorner ? Material.SEA_LANTERN
                        : (isEdge ? Material.PRISMARINE_BRICKS : Material.DARK_PRISMARINE);
                break;
            case "soulblade":
                mat = isCorner ? Material.NETHERITE_BLOCK : (isEdge ? Material.BLACKSTONE : Material.SOUL_SOIL);
                break;
            default:
                mat = Material.STONE_BRICKS;
                break;
        }
        loc.getBlock().setType(mat);
    }

    private void applyDecorations(Location center, String type) {
        switch (type) {
            case "warden":
                placeBlock(center, 1, 0, 1, Material.CYAN_CANDLE);
                placeBlock(center, -1, 0, -1, Material.CYAN_CANDLE);
                placeBlock(center, 2, 0, 2, Material.SCULK_SENSOR);
                placeBlock(center, -2, 0, -2, Material.SCULK_SENSOR);
                break;
            case "nether":
                placeBlock(center, 2, 0, 0, Material.SOUL_FIRE);
                placeBlock(center, -2, 0, 0, Material.SOUL_FIRE);
                placeBlock(center, 0, 0, 2, Material.SOUL_FIRE);
                placeBlock(center, 0, 0, -2, Material.SOUL_FIRE);
                break;
            case "end":
                placeBlock(center, 2, 0, 2, Material.END_ROD);
                placeBlock(center, -2, 0, -2, Material.END_ROD);
                placeBlock(center, 2, 0, -2, Material.END_ROD);
                placeBlock(center, -2, 0, 2, Material.END_ROD);
                break;
            case "ghostblade":
                placeBlock(center, 1, 0, 1, Material.SOUL_LANTERN);
                placeBlock(center, -1, 0, -1, Material.SOUL_LANTERN);
                placeBlock(center, 2, 0, 0, Material.SOUL_FIRE);
                break;
            case "dragonblade":
                placeBlock(center, 2, 0, 2, Material.PURPLE_STAINED_GLASS);
                placeBlock(center, -2, 0, -2, Material.PURPLE_STAINED_GLASS);
                placeBlock(center, 2, 1, 2, Material.PURPLE_STAINED_GLASS);
                placeBlock(center, -2, 1, -2, Material.PURPLE_STAINED_GLASS);
                break;
            case "mistblade":
                placeBlock(center, 1, 0, 0, Material.PRISMARINE_WALL);
                placeBlock(center, -1, 0, 0, Material.PRISMARINE_WALL);
                placeBlock(center, 0, 0, 1, Material.PRISMARINE_WALL);
                placeBlock(center, 0, 0, -1, Material.PRISMARINE_WALL);
                break;
            case "soulblade":
                placeBlock(center, 2, 0, 2, Material.SOUL_LANTERN);
                placeBlock(center, -2, 0, -2, Material.SOUL_LANTERN);
                break;
        }
    }

    private void placeBlock(Location center, int x, int y, int z, Material mat) {
        center.clone().add(x, y, z).getBlock().setType(mat);
    }
}
