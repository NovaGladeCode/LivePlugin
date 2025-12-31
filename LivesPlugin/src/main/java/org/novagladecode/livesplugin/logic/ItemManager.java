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

        registerGhostbladeRecipe();
        registerDragonbladeRecipe();
        registerMistbladeRecipe();
        registerSoulbladeRecipe();
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

    public void registerGhostbladeRecipe() {
        ItemStack item = createGhostblade();
        NamespacedKey key = new NamespacedKey(plugin, "ghostblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("TNS", "SWS", "SNT");
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.SOUL_SAND);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipe);
    }

    public void registerDragonbladeRecipe() {
        ItemStack item = createDragonblade();
        NamespacedKey key = new NamespacedKey(plugin, "dragonblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("HBH", "BWB", "HBH");
        recipe.setIngredient('H', Material.DRAGON_HEAD);
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipe);
    }

    public void registerMistbladeRecipe() {
        ItemStack item = createMistblade();
        NamespacedKey key = new NamespacedKey(plugin, "mistblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("TBT", "BWB", "TBT");
        recipe.setIngredient('T', Material.TRIDENT);
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipe);
    }

    public void registerSoulbladeRecipe() {
        ItemStack item = createSoulblade();
        NamespacedKey key = new NamespacedKey(plugin, "soulblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("WBW", "BWB", "WBW");
        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);
        // Using W again for the sword in the middle might be confusing in the shape
        // string, let's use 'S' for sword and 'X' for skull
        recipe.shape("XNB", "NSW", "BNX");
        recipe.setIngredient('X', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);
        recipe.setIngredient('S', Material.NETHERITE_SWORD);
        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL); // Duplicate to match shape
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
        meta.setDisplayName("§3§lWarden Mace");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7Forged from the heart of the §3Deep Dark§7,");
        lore.add("§7this weapon echoes with ancient power.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Sonic Boom on crit (10% chance, 5+ block fall)");
        lore.add("§f⬥ §7Shield Stun on hit");
        lore.add("");
        lore.add("§b§lABILITIES:");
        lore.add("§3/wardenmace 1 §8» §fSonic Wave");
        lore.add("  §7Emit a devastating shockwave");
        lore.add("§3/wardenmace 2 §8» §fWarden's Grasp");
        lore.add("  §7Pull nearby enemies toward you");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        mace.setItemMeta(meta);
        return mace;
    }

    public ItemStack createNetherMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        meta.setDisplayName("§c§lNether Mace");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7Forged in the §cinfernal depths§7 of the Nether,");
        lore.add("§7this weapon burns with hellish fury.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Fire Immunity (all sources)");
        lore.add("§f⬥ §7Effect Immunity (Wither/Poison/Magic)");
        lore.add("§f⬥ §7Shield Stun on hit");
        lore.add("");
        lore.add("§c§lABILITIES:");
        lore.add("§6/nethermace 1 §8» §fInfernal Wrath");
        lore.add("  §7Unleash a fiery explosion");
        lore.add("§6/nethermace 2 §8» §fFire Tornado");
        lore.add("  §7Create a devastating fire vortex");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        mace.setItemMeta(meta);
        customNetherMace = mace;
        return mace;
    }

    public ItemStack createEndMace() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5§lEnd Mace");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7Forged from the §5endless void§7 itself,");
        lore.add("§7this weapon defies the laws of reality.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Fall Damage Immunity");
        lore.add("§f⬥ §7Dragon Breath Immunity");
        lore.add("§f⬥ §7Shield Stun on hit");
        lore.add("");
        lore.add("§5§lABILITIES:");
        lore.add("§d/endmace 1 §8» §fVoid Cloak");
        lore.add("  §7Become completely invisible (10s)");
        lore.add("§d/endmace 2 §8» §fSingularity");
        lore.add("  §7Pull and launch enemies away");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.BREACH, 3, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
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
        meta.setDisplayName("§7§lGhostblade");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7A spectral blade forged from the");
        lore.add("§7whispers of §frestless souls§7.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Complete Invisibility while holding");
        lore.add("");
        lore.add("§7§lABILITIES:");
        lore.add("§f/ghostblade 1 §8» §fHaunt");
        lore.add("  §7Possess and disorient your target");
        lore.add("§f/ghostblade 2 §8» §fSpectral Pull");
        lore.add("  §7Yank enemies toward you");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createDragonblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§6§lDragonblade");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7Forged in the §6fiery breath§7 of");
        lore.add("§7the §5Ender Dragon§7 itself.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Fall Damage Immunity while holding");
        lore.add("");
        lore.add("§6§lABILITIES:");
        lore.add("§e/dragonblade 1 §8» §fDragon Leap");
        lore.add("  §7Launch yourself into the air");
        lore.add("§e/dragonblade 2 §8» §fDragon Strike");
        lore.add("  §7Slam down with explosive force");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createMistblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§b§lMistblade");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7A blade forged from §bpure ocean mist§7,");
        lore.add("§7blessed by the §3sea guardians§7.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Dolphin's Grace while holding");
        lore.add("");
        lore.add("§b§lABILITIES:");
        lore.add("§3/mistblade 1 §8» §fTrident Storm");
        lore.add("  §7Summon a barrage of tridents");
        lore.add("§3/mistblade 2 §8» §fTidal Surge");
        lore.add("  §7Create a powerful water wave");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createSoulblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§8§lSoulblade");
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7A dark blade that §8consumes souls§7,");
        lore.add("§7growing stronger with each life taken.");
        lore.add("");
        lore.add("§6§lPASSIVES:");
        lore.add("§f⬥ §7Strength I while holding");
        lore.add("");
        lore.add("§8§lABILITIES:");
        lore.add("§7/soulblade 1 §8» §fSoul Beam");
        lore.add("  §7Fire a devastating soul projectile");
        lore.add("§7/soulblade 2 §8» §fSoul Devour");
        lore.add("  §7Drain the life force of nearby enemies");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createSacredForge(String weaponType) {
        if (weaponType == null)
            return null;
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();
        String name = (weaponType.substring(0, 1).toUpperCase() + weaponType.substring(1).toLowerCase() + " Forge");
        meta.setDisplayName("§6§l" + name);
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("§7A §6sacred station§7 for forging");
        lore.add("§7legendary weapons of immense power.");
        lore.add("");
        lore.add("§eSpecialized for: §b" + weaponType);
        lore.add("");
        lore.add("§7Place this in the world to create");
        lore.add("§7a §67x7 Forge Structure§7 for rituals.");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━━━━");
        meta.setLore(lore);

        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

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
        return "§5§lEnd Mace".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isWardenMaceAnywhere() {
        return isMaceWithNameAnywhere("§3§lWarden Mace");
    }

    public boolean isNetherMaceAnywhere() {
        return isMaceWithNameAnywhere("§c§lNether Mace");
    }

    public boolean isEndMaceAnywhere() {
        return isMaceWithNameAnywhere("§5§lEnd Mace");
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
                && "§7§lGhostblade".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isDragonblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§6§lDragonblade".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isMistblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§b§lMistblade".equals(item.getItemMeta().getDisplayName());
    }

    public boolean isSoulblade(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()
                && "§8§lSoulblade".equals(item.getItemMeta().getDisplayName());
    }
}
