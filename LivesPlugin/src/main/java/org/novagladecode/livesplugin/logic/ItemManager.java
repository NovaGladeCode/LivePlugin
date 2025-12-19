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

        registerWardenMaceRecipe();
        registerNetherMaceRecipe();
        registerEndMaceRecipe();
        registerChickenBowRecipe();
        registerGhostbladeRecipe();
        registerDragonbladeRecipe();
        registerMistbladeRecipe();
        registerSoulbladeRecipe();
        registerForgeCompassRecipe();
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
        recipe.shape("LHL", "CMC", "LSL");
        recipe.setIngredient('L', Material.SCULK_CATALYST);
        recipe.setIngredient('H', Material.ECHO_SHARD); // Warden Heart substitute
        recipe.setIngredient('C', Material.SCULK_SENSOR);
        recipe.setIngredient('M', Material.HEAVY_CORE);
        recipe.setIngredient('S', Material.NETHERITE_SWORD); // Shaft/Handle requirement
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("warden", Material.SCULK_CATALYST);
    }

    public void registerNetherMaceRecipe() {
        ItemStack netherMace = createNetherMace();
        NamespacedKey key = new NamespacedKey(plugin, "nether_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, netherMace);
        recipe.shape("BSB", "NMN", "BIB");
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('S', Material.NAUTILUS_SHELL); // Wither Heart
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('M', Material.HEAVY_CORE);
        recipe.setIngredient('I', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("nether", Material.CRYING_OBSIDIAN);
    }

    public void registerEndMaceRecipe() {
        createEndMace();
        NamespacedKey key = new NamespacedKey(plugin, "end_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, customEndMace);
        recipe.shape("DHD", "EME", "DSD");
        recipe.setIngredient('D', Material.DRAGON_BREATH);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA); // Dragon Heart
        recipe.setIngredient('E', Material.DRAGON_HEAD);
        recipe.setIngredient('M', Material.HEAVY_CORE);
        recipe.setIngredient('S', Material.BREEZE_ROD);
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("end", Material.PURPUR_BLOCK);
    }

    public void registerChickenBowRecipe() {
        ItemStack item = createChickenBow();
        NamespacedKey key = new NamespacedKey(plugin, "chicken_bow");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("EFF", "EBF", "EFF");
        recipe.setIngredient('E', Material.EGG);
        recipe.setIngredient('F', Material.FEATHER);
        recipe.setIngredient('B', Material.BOW);
        Bukkit.addRecipe(recipe);
    }

    public void registerGhostbladeRecipe() {
        ItemStack item = createGhostblade();
        NamespacedKey key = new NamespacedKey(plugin, "ghostblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("TNT", "SWS", "TNT");
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.SOUL_SAND);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("ghostblade", Material.SOUL_SAND);
    }

    public void registerDragonbladeRecipe() {
        ItemStack item = createDragonblade();
        NamespacedKey key = new NamespacedKey(plugin, "dragonblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("EHE", "NWN", "EBE");
        recipe.setIngredient('E', Material.DRAGON_HEAD);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA); // Dragon Heart
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("dragonblade", Material.OBSIDIAN);
    }

    public void registerMistbladeRecipe() {
        ItemStack item = createMistblade();
        NamespacedKey key = new NamespacedKey(plugin, "mistblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("TBT", "PWP", "TBT");
        recipe.setIngredient('T', Material.TRIDENT);
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);
        recipe.setIngredient('P', Material.PRISMARINE_BRICKS);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("mistblade", Material.PRISMARINE_BRICKS);
    }

    public void registerSoulbladeRecipe() {
        ItemStack item = createSoulblade();
        NamespacedKey key = new NamespacedKey(plugin, "soulblade");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("XHX", "NWS", "XNX");
        recipe.setIngredient('X', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('H', Material.NAUTILUS_SHELL); // Wither Heart
        recipe.setIngredient('N', Material.NETHERITE_BLOCK);
        recipe.setIngredient('S', Material.SOUL_SAND);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipe);
        registerSacredForgeRecipe("soulblade", Material.BLACKSTONE);
    }

    private void registerSacredForgeRecipe(String type, Material thematicMaterial) {
        ItemStack forge = createSacredForge(type);
        NamespacedKey key = new NamespacedKey(plugin, type + "_forge");
        ShapedRecipe recipe = new ShapedRecipe(key, forge);
        recipe.shape("MMM", "MCM", "MMM");
        recipe.setIngredient('M', thematicMaterial);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        Bukkit.addRecipe(recipe);
    }

    public void registerForgeCompassRecipe() {
        ItemStack compass = createForgeCompass();
        NamespacedKey key = new NamespacedKey(plugin, "forge_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape(" D ", "DCD", " D ");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('C', Material.COMPASS);
        Bukkit.addRecipe(recipe);
    }

    public ItemStack createForgeCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lForge Compass");
        meta.setLore(Arrays.asList("§7Right-click to point to", "§7a random Sacred Forge!"));
        item.setItemMeta(meta);
        return item;
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
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oForged from the heart of the deep dark...");
        lore.add("");
        lore.add("§6§lLegendary Weapon");
        lore.add("§7Passives:");
        lore.add("§b- Echo Sense: §7Gain night vision in caves");
        lore.add("");
        lore.add("§b§lAbilities:");
        lore.add("§b/wardenmace 1 §8| §fSonic Wave");
        lore.add("§b/wardenmace 2 §8| §fWarden's Grasp");
        meta.setLore(lore);
        mace.setItemMeta(meta);
        return mace;
    }

    public ItemStack createNetherMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        meta.setDisplayName("§c§lNether Mace");
        meta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oForged in the boiling rivers of the Nether...");
        lore.add("");
        lore.add("§6§lLegendary Weapon");
        lore.add("§7Passives:");
        lore.add("§c- Infernal Body: §7Immune to Fire & Wither");
        lore.add("");
        lore.add("§c§lAbilities:");
        lore.add("§6/nethermace 1 §8| §fInfernal Wrath");
        lore.add("§6/nethermace 2 §8| §fFire Tornado");
        meta.setLore(lore);
        mace.setItemMeta(meta);
        customNetherMace = mace;
        return mace;
    }

    public ItemStack createEndMace() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5§lEnd Mace");
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.BREACH, 3, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oFormed from the silent essence of the void...");
        lore.add("");
        lore.add("§6§lLegendary Weapon");
        lore.add("§7Passives:");
        lore.add("§d- Void Shield: §7Immune to Fall & Dragon Breath");
        lore.add("");
        lore.add("§d§lAbilities:");
        lore.add("§5/endmace 1 §8| §fVoid Cloak");
        lore.add("§5/endmace 2 §8| §fSingularity");
        meta.setLore(lore);
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
        meta.setDisplayName("§f§lGhostblade");
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 5, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oA spectral blade that exists between worlds.");
        lore.add("");
        lore.add("§6§lLegendary Blade");
        lore.add("§7Passives:");
        lore.add("§f- Ethereal: §7Invisibility while holding");
        lore.add("");
        lore.add("§f§lAbilities:");
        lore.add("§7/ghostblade 1 §8| §fHaunt");
        lore.add("§7/ghostblade 2 §8| §fSpectral Pull");
        meta.setLore(lore);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createDragonblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§6§lDragonblade");
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.KNOCKBACK, 2, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oForged in the dying breath of a King.");
        lore.add("");
        lore.add("§6§lLegendary Blade");
        lore.add("§7Passives:");
        lore.add("§e- Dragon Flight: §7Immune to fall damage");
        lore.add("");
        lore.add("§e§lAbilities:");
        lore.add("§7/dragonblade 1 §8| §fDragon Leap");
        lore.add("§7/dragonblade 2 §8| §fDragon Strike");
        meta.setLore(lore);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createMistblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§b§lMistblade");
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oA blade woven from sea spray and tidal force.");
        lore.add("");
        lore.add("§6§lLegendary Blade");
        lore.add("§7Passives:");
        lore.add("§b- Sea Grace: §7Dolphin's Grace while holding");
        lore.add("");
        lore.add("§b§lAbilities:");
        lore.add("§7/mistblade 1 §8| §fTrident Storm");
        lore.add("§7/mistblade 2 §8| §fTidal Surge");
        meta.setLore(lore);
        sword.setItemMeta(meta);
        return sword;
    }

    public ItemStack createSoulblade() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("§8§lSoulblade");
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.BANE_OF_ARTHROPODS, 1, true); // Visual glint
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§8§oIt hungers for the quintessence of its master.");
        lore.add("");
        lore.add("§6§lLegendary Blade");
        lore.add("§7Passives:");
        lore.add("§8- Soul Empowerment: §7Continuous Strength I");
        lore.add("");
        lore.add("§8§lAbilities:");
        lore.add("§7/soulblade 1 §8| §fSoul Beam");
        lore.add("§7/soulblade 2 §8| §fSoul Devour");
        meta.setLore(lore);
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
                && "§f§lGhostblade".equals(item.getItemMeta().getDisplayName());
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
