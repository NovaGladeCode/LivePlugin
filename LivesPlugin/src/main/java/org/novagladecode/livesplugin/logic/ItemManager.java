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
        recipe.shape(" N ", "NLN", " N ");
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('L', Material.NETHER_STAR); // Level Item (uses nether star as base)

        Bukkit.addRecipe(recipe);
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
}
