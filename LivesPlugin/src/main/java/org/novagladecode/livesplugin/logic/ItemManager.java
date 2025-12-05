package org.novagladecode.livesplugin.logic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class ItemManager {

    private final JavaPlugin plugin;

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerUnbanRecipe() {
        ItemStack unbanItem = createUnbanItem();

        NamespacedKey key = new NamespacedKey(plugin, "unban_item");
        ShapedRecipe recipe = new ShapedRecipe(key, unbanItem);
        recipe.shape("DND", "NLN", "DND");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('L', Material.NETHER_STAR); // We check for name in CraftItemEvent if needed, but for now
                                                         // vanilla crafting accepts any item of that material

        Bukkit.addRecipe(recipe);
    }

    public void registerWardenMaceRecipe() {
        ItemStack wardenMace = createWardenMace();

        NamespacedKey key = new NamespacedKey(plugin, "warden_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, wardenMace);
        // "very top of crafting table" (Heart) "normal mace in the middle", "diamond
        // block around the rest"
        recipe.shape("DHD", "DMD", "DDD");

        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('M', Material.MACE);

        // Use Echo Shard as the base ingredient, but we'll need to enforce the custom
        // meta in a PrepareItemCraftEvent
        // For the recipe registration, we just specify the material.
        recipe.setIngredient('H', Material.ECHO_SHARD);

        Bukkit.addRecipe(recipe);
    }

    public ItemStack createWardenHeart() {
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3Warden Heart");
        meta.setLore(Arrays.asList("§7A pulsating heart dropped", "§7by the deep dark guardian."));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createWardenMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        meta.setDisplayName("§3Warden Mace");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7Forged from the heart of the deep dark...");
        lore.add("§7Abilities:");
        lore.add("§b/wardenmace 1 §7- Sonic Shockwave");
        lore.add("§b/wardenmace 2 §7- Sonic Beam");
        meta.setLore(lore);
        mace.setItemMeta(meta);
        return mace;
    }

    public ItemStack createLevelItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Level Item");
        meta.setLore(Arrays.asList("§7Right-click to gain a level!"));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createUnbanItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5Unban Token");
        meta.setLore(Arrays.asList("§7Right-click to open unban menu."));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isLevelItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§6Level Item");
    }

    public boolean isUnbanItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§5Unban Token");
    }

    public boolean isWardenHeart(ItemStack item) {
        if (item == null || item.getType() != Material.ECHO_SHARD)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§3Warden Heart");
    }
}
