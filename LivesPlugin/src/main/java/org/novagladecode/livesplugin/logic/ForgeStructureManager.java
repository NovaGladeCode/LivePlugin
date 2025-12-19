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

        // Clear area 7x7x6
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = 0; y <= 5; y++) {
                    center.clone().add(x, y, z).getBlock().setType(Material.AIR);
                }
            }
        }

        // Base 7x7
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Location loc = center.clone().add(x, -1, z);
                applyBaseBlock(loc, type, x, z);
            }
        }

        // Structure (Pillars and Rim)
        buildVerticalStructure(center, type);

        // Decorations
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
        boolean isCorner = (Math.abs(relX) == 3 && Math.abs(relZ) == 3);
        boolean isEdge = (Math.abs(relX) == 3 || Math.abs(relZ) == 3);
        boolean isInnerRim = (Math.abs(relX) == 2 && Math.abs(relZ) == 2);

        switch (type) {
            case "warden":
                mat = isCorner ? Material.SCULK_SHRIEKER : (isEdge ? Material.SCULK_CATALYST : Material.SCULK);
                if (isInnerRim)
                    mat = Material.SCULK_CATALYST;
                break;
            case "nether":
                mat = isCorner ? Material.CRYING_OBSIDIAN : (isEdge ? Material.NETHERITE_BLOCK : Material.MAGMA_BLOCK);
                if (isInnerRim)
                    mat = Material.GLOWSTONE;
                break;
            case "end":
                mat = isCorner ? Material.OBSIDIAN : (isEdge ? Material.END_STONE_BRICKS : Material.PURPUR_BLOCK);
                if (isInnerRim)
                    mat = Material.PURPUR_PILLAR;
                break;
            case "ghostblade":
                mat = isCorner ? Material.SOUL_SOIL : (isEdge ? Material.SOUL_SAND : Material.SOUL_SOIL);
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

    private void buildVerticalStructure(Location center, String type) {
        Material pillarMat = Material.STONE_BRICKS;
        Material rimMat = Material.STONE_BRICK_SLAB;

        switch (type) {
            case "warden":
                pillarMat = Material.SCULK_CATALYST;
                rimMat = Material.SCULK_VEIN;
                break;
            case "nether":
                pillarMat = Material.NETHER_BRICKS;
                rimMat = Material.NETHER_BRICK_FENCE;
                break;
            case "end":
                pillarMat = Material.PURPUR_PILLAR;
                rimMat = Material.END_STONE_BRICK_WALL;
                break;
            case "ghostblade":
                pillarMat = Material.SOUL_SAND;
                rimMat = Material.POLISHED_BLACKSTONE_BRICK_WALL;
                break;
            case "dragonblade":
                pillarMat = Material.OBSIDIAN;
                rimMat = Material.CRYING_OBSIDIAN;
                break;
            case "mistblade":
                pillarMat = Material.PRISMARINE_BRICKS;
                rimMat = Material.PRISMARINE_WALL;
                break;
            case "soulblade":
                pillarMat = Material.POLISHED_BLACKSTONE_BRICKS;
                rimMat = Material.NETHERITE_BLOCK;
                break;
        }

        // 4 Pillars
        int[] pos = { -3, 3 };
        for (int x : pos) {
            for (int z : pos) {
                for (int y = 0; y <= 3; y++) {
                    placeBlock(center, x, y, z, pillarMat);
                }
                // Cap them
                placeBlock(center, x, 4, z, Material.GLOWSTONE);
            }
        }

        // Connecting Arches (Upper Rim at y=4)
        for (int i = -3; i <= 3; i++) {
            placeBlock(center, i, 4, 3, pillarMat);
            placeBlock(center, i, 4, -3, pillarMat);
            placeBlock(center, 3, 4, i, pillarMat);
            placeBlock(center, -3, 4, i, pillarMat);
        }
    }

    private void applyDecorations(Location center, String type) {
        switch (type) {
            case "warden":
                placeBlock(center, 1, 0, 1, Material.CYAN_CANDLE);
                placeBlock(center, -1, 0, -1, Material.CYAN_CANDLE);
                placeBlock(center, 3, 1, 3, Material.SCULK_SENSOR);
                placeBlock(center, -3, 1, -3, Material.SCULK_SENSOR);
                placeBlock(center, 3, 1, -3, Material.SCULK_SENSOR);
                placeBlock(center, -3, 1, 3, Material.SCULK_SENSOR);
                break;
            case "nether":
                placeBlock(center, 2, 0, 2, Material.SOUL_FIRE);
                placeBlock(center, -2, 0, -2, Material.SOUL_FIRE);
                placeBlock(center, 2, 0, -2, Material.SOUL_FIRE);
                placeBlock(center, -2, 0, 2, Material.SOUL_FIRE);
                placeBlock(center, 0, 5, 0, Material.CRYING_OBSIDIAN);
                break;
            case "end":
                placeBlock(center, 3, 5, 3, Material.END_ROD);
                placeBlock(center, -3, 5, -3, Material.END_ROD);
                placeBlock(center, 3, 5, -3, Material.END_ROD);
                placeBlock(center, -3, 5, 3, Material.END_ROD);
                placeBlock(center, 0, 4, 0, Material.DRAGON_EGG);
                break;
            case "ghostblade":
                placeBlock(center, 2, 0, 2, Material.SOUL_LANTERN);
                placeBlock(center, -2, 0, -2, Material.SOUL_LANTERN);
                placeBlock(center, 3, 5, 0, Material.SOUL_FIRE);
                break;
            case "dragonblade":
                placeBlock(center, 3, 0, 3, Material.DRAGON_WALL_HEAD);
                placeBlock(center, -3, 0, -3, Material.DRAGON_WALL_HEAD);
                placeBlock(center, 0, 5, 0, Material.GOLD_BLOCK);
                break;
            case "mistblade":
                placeBlock(center, 2, 0, 2, Material.SEA_LANTERN);
                placeBlock(center, -2, 0, -2, Material.SEA_LANTERN);
                placeBlock(center, 2, 0, -2, Material.SEA_LANTERN);
                placeBlock(center, -2, 0, 2, Material.SEA_LANTERN);
                break;
            case "soulblade":
                placeBlock(center, 3, 1, 3, Material.WITHER_SKELETON_SKULL);
                placeBlock(center, -3, 1, -3, Material.WITHER_SKELETON_SKULL);
                placeBlock(center, 0, 5, 0, Material.NETHERITE_BLOCK);
                break;
        }
    }

    private void placeBlock(Location center, int x, int y, int z, Material mat) {
        center.clone().add(x, y, z).getBlock().setType(mat);
    }
}
