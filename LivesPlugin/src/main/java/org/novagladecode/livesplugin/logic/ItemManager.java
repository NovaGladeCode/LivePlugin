package org.novagladecode.livesplugin.logic;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataType;

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
        recipe.setIngredient('H', Material.NAUTILUS_SHELL); // Wither Heart now
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

    public ItemStack createGhostblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§7Ghostblade");
        List<String> lore = new ArrayList<>();
        lore.add("§7A spectral blade from the beyond.");
        lore.add("§7Passives:");
        lore.add("§f- Invisibility §7while holding");
        lore.add("§7Abilities:");
        lore.add("§7/ghostblade 1 (§fRight-Click§7) - Haunt");
        lore.add("§7/ghostblade 2 (§fShift+Right-Click§7) - Spectral Pull");
        meta.setLore(lore);
        meta.setUnbreakable(true);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createDragonblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§6Dragonblade");
        List<String> lore = new ArrayList<>();
        lore.add("§7Forged in the breath of a dragon.");
        lore.add("§7Passives:");
        lore.add("§f- No Fall Damage §7while holding");
        lore.add("§7Abilities:");
        lore.add("§7/dragonblade 1 (§fRight-Click§7) - Dragon Leap");
        lore.add("§7/dragonblade 2 (§fShift+Right-Click§7) - Dragon Strike");
        meta.setLore(lore);
        meta.setUnbreakable(true);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createMistblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§bMistblade");
        List<String> lore = new ArrayList<>();
        lore.add("§7A blade of pure condensation.");
        lore.add("§7Passives:");
        lore.add("§f- Dolphin's Grace §7while holding");
        lore.add("§7Abilities:");
        lore.add("§7/mistblade 1 (§fRight-Click§7) - Trident Storm");
        lore.add("§7/mistblade 2 (§fShift+Right-Click§7) - Tidal Surge");
        meta.setLore(lore);
        meta.setUnbreakable(true);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createSoulblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§8Soulblade");
        List<String> lore = new ArrayList<>();
        lore.add("§7Consumes the very essence of the wielder.");
        lore.add("§7Passives:");
        lore.add("§f- Strength I §7while holding");
        lore.add("§7Abilities:");
        lore.add("§7/soulblade 1 (§fRight-Click§7) - Soul Beam");
        lore.add("§7/soulblade 2 (§fShift+Right-Click§7) - Soul Devour");
        meta.setLore(lore);
        meta.setUnbreakable(true);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createSacredForge() {
        return createSacredForge(null);
    }

    public ItemStack createSacredForge(String weaponType) {
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();
        String name = weaponType == null ? "Sacred Forge"
                : (weaponType.substring(0, 1).toUpperCase() + weaponType.substring(1) + " Forge");
        meta.setDisplayName("§6§l" + name);
        List<String> lore = new ArrayList<>();
        lore.add("§7A station for forging legendary weapons.");
        if (weaponType != null)
            lore.add("§eSpecialized for: §b" + weaponType);
        lore.add("§7Place this in the world to allow rituals.");
        meta.setLore(lore);

        NamespacedKey key = new NamespacedKey(plugin, "sacred_forge");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        if (weaponType != null) {
            NamespacedKey typeKey = new NamespacedKey(plugin, "forge_type");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, weaponType.toLowerCase());
        }

        item.setItemMeta(meta);
        return item;
    }

    public boolean isSacredForge(ItemStack item) {
        if (item == null || item.getType() != Material.CRAFTING_TABLE || !item.hasItemMeta())
            return false;
        NamespacedKey key = new NamespacedKey(plugin, "sacred_forge");
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public String getForgeType(ItemStack item) {
        if (!isSacredForge(item))
            return null;
        NamespacedKey typeKey = new NamespacedKey(plugin, "forge_type");
        return item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
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
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8Wither Heart");
        meta.setLore(Arrays.asList("§7A dark shell dropped", "§7by the Wither."));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isWitherHeart(ItemStack item) {
        if (item == null || item.getType() != Material.NAUTILUS_SHELL)
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

    public boolean isWardenMaceAnywhere() {
        return isMaceWithNameAnywhere("§3Warden Mace");
    }

    public boolean isNetherMaceAnywhere() {
        return isMaceWithNameAnywhere("§cNether Mace");
    }

    public boolean isEndMaceAnywhere() {
        return isMaceWithNameAnywhere("§5End Mace");
    }

    private boolean isMaceWithNameAnywhere(String name) {
        // Check all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : p.getInventory()) {
                if (isMaceWithDisplayName(item, name))
                    return true;
            }
            for (ItemStack item : p.getEnderChest()) {
                if (isMaceWithDisplayName(item, name))
                    return true;
            }
        }
        // Check dropped items
        for (World w : Bukkit.getWorlds()) {
            for (Item entity : w.getEntitiesByClass(Item.class)) {
                if (isMaceWithDisplayName(entity.getItemStack(), name))
                    return true;
            }
        }
        return false;
    }

    private boolean isMaceWithDisplayName(ItemStack item, String name) {
        return item != null && item.getType() == Material.MACE && item.hasItemMeta()
                && name.equals(item.getItemMeta().getDisplayName());
    }

    public boolean isGhostblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§7Ghostblade".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isDragonblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§6Dragonblade".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isMistblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§bMistblade".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isSoulblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§8Soulblade".equals(item.getItemMeta().getDisplayName());
    }
}
