package org.novagladecode.livesplugin.logic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemManager {

    private final JavaPlugin plugin;
    public static ItemStack customNetherMace;
    public static ItemStack customEndMace;

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        createNetherMace();
        createEndMace();
    }

    public void registerUnbanRecipe() {
        ItemStack unbanItem = createUnbanItem();

        NamespacedKey key = new NamespacedKey(plugin, "unban_item");
        ShapedRecipe recipe = new ShapedRecipe(key, unbanItem);
        recipe.shape("DND", "NLN", "DND");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('L', Material.NETHER_STAR);

        Bukkit.addRecipe(recipe);
    }

    public void registerWardenMaceRecipe() {
        ItemStack wardenMace = createWardenMace();
        NamespacedKey key = new NamespacedKey(plugin, "warden_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, wardenMace);
        recipe.shape("CSC", "SHS", "CMC");
        recipe.setIngredient('C', Material.SCULK_CATALYST);
        recipe.setIngredient('S', Material.SCULK_SENSOR);
        recipe.setIngredient('H', Material.ECHO_SHARD); // Warden Heart
        recipe.setIngredient('M', Material.HEAVY_CORE);
        Bukkit.addRecipe(recipe);
    }

    public void registerNetherMaceRecipe() {
        ItemStack netherMace = createNetherMace();
        NamespacedKey key = new NamespacedKey(plugin, "nether_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, netherMace);
        recipe.shape("ISI", "SHS", "BMB");
        recipe.setIngredient('I', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.NETHERITE_SCRAP);
        recipe.setIngredient('H', Material.NETHER_STAR); // Wither Heart
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('M', Material.HEAVY_CORE);
        Bukkit.addRecipe(recipe);
    }

    public void registerEndMaceRecipe() {
        createEndMace();
        NamespacedKey key = new NamespacedKey(plugin, "end_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, customEndMace);
        recipe.shape("PCP", "CHC", "BMB");
        recipe.setIngredient('P', Material.POPPED_CHORUS_FRUIT);
        recipe.setIngredient('C', Material.DRAGON_HEAD);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA); // Dragon Heart
        recipe.setIngredient('B', Material.BREEZE_ROD);
        recipe.setIngredient('M', Material.HEAVY_CORE);
        Bukkit.addRecipe(recipe);
    }

    public void registerChickenBowRecipe() {
        ItemStack item = createChickenBow();
        NamespacedKey key = new NamespacedKey(plugin, "chicken_bow");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("DFD", "FBF", "DFD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('F', Material.FEATHER);
        recipe.setIngredient('B', Material.BOW);
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
        List<String> lore = new ArrayList<>();
        lore.add("§7Forged from the heart of the deep dark...");
        lore.add("§7Abilities:");
        lore.add("§b/wardenmace 1 §7- Sonic Wave");
        lore.add("§b/wardenmace 2 §7- Warden's Grasp");
        meta.setLore(lore);
        mace.setItemMeta(meta);
        return mace;
    }

    public ItemStack createNetherMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        meta.setDisplayName("§cNether Mace");
        List<String> lore = new ArrayList<>();
        lore.add("§7Forged in the depths of the Nether...");
        lore.add("§7Abilities:");
        lore.add("§6/nethermace 1 §7- Infernal Wrath");
        lore.add("§6/nethermace 2 §7- Fire Tornado");
        meta.setLore(lore);
        mace.setItemMeta(meta);
        customNetherMace = mace;
        return mace;
    }

    public ItemStack createEndMace() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5End Mace");
        List<String> lore = new ArrayList<>();
        lore.add("§7Formed from the void itself");
        lore.add("§7Abilities:");
        lore.add("§5/endmace 1 §7- Void Cloak");
        lore.add("§5/endmace 2 §7- Singularity");
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.BREACH, 3, true);
        item.setItemMeta(meta);
        customEndMace = item;
        return item;
    }

    public ItemStack createChickenBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName("§eChicken Bow");
        List<String> lore = new ArrayList<>();
        lore.add("§7Cluck cluck... boom.");
        lore.add("§7Abilities:");
        lore.add("§e50% Chance: §7Slow Falling (15s)");
        lore.add("§c40% Chance: §7Summon Deadly Chicken");
        meta.setLore(lore);
        bow.setItemMeta(meta);
        return bow;
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

    public ItemStack createWitherHeart() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8Wither Heart");
        meta.setLore(Arrays.asList("§7A dark star dropped", "§7by the Wither."));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isWitherHeart(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§8Wither Heart");
    }

    public ItemStack createDragonHeart() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5Dragon Heart");
        meta.setLore(Arrays.asList("§7The pulsing heart of", "§7the Ender Dragon."));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isDragonHeart(ItemStack item) {
        if (item == null || item.getType() != Material.HEART_OF_THE_SEA)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§5Dragon Heart");
    }

    public boolean isEndMace(ItemStack item) {
        if (item == null || item.getType() != Material.MACE || !item.hasItemMeta())
            return false;
        return "§5End Mace".equals(item.getItemMeta().getDisplayName());
    }
}
