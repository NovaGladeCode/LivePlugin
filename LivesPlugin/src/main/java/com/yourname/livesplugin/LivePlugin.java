package com.yourname.livesplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LivesPlugin extends JavaPlugin implements Listener {
    
    private File dataFile;
    private FileConfiguration dataConfig;
    private Map<UUID, Boolean> invisibilityToggle = new HashMap<>();
    private NamespacedKey levelKey;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        levelKey = new NamespacedKey(this, "level");
        
        // Load data file
        dataFile = new File(getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveResource("playerdata.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        getLogger().info("Lives Plugin has been enabled!");
        
        // Apply effects to all online players
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                applyEffects(p);
            }
        }, 0L, 100L);
    }
    
    @Override
    public void onDisable() {
        saveData();
        getLogger().info("Lives Plugin has been disabled!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!dataConfig.contains(p.getUniqueId().toString())) {
            dataConfig.set(p.getUniqueId() + ".lives", 5);
            dataConfig.set(p.getUniqueId() + ".level", 0);
            dataConfig.set(p.getUniqueId() + ".banned", false);
            saveData();
        }
        
        // Check if banned
        if (dataConfig.getBoolean(p.getUniqueId() + ".banned")) {
            p.kickPlayer("§cYou are banned! Someone must craft an Unban Item to revive you.");
        } else {
            applyEffects(p);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        
        UUID victimUUID = victim.getUniqueId();
        int lives = dataConfig.getInt(victimUUID + ".lives");
        int level = dataConfig.getInt(victimUUID + ".level");
        
        // Drop level item if they have any levels
        if (level > 0) {
            ItemStack levelItem = createLevelItem();
            victim.getWorld().dropItemNaturally(victim.getLocation(), levelItem);
            
            // Decrease level
            level--;
            dataConfig.set(victimUUID + ".level", level);
        }
        
        // Decrease lives
        lives--;
        dataConfig.set(victimUUID + ".lives", lives);
        
        if (lives <= 0) {
            dataConfig.set(victimUUID + ".banned", true);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                victim.kickPlayer("§cYou have lost all your lives! You are banned until someone crafts an Unban Item.");
            }, 5L);
        }
        
        saveData();
        victim.sendMessage("§cYou died! Lives remaining: " + lives);
        
        // Give killer a level item and increase their level
        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            int killerLevel = dataConfig.getInt(killerUUID + ".level");
            killerLevel++;
            dataConfig.set(killerUUID + ".level", killerLevel);
            
            ItemStack levelItem = createLevelItem();
            killer.getInventory().addItem(levelItem);
            killer.sendMessage("§aYou killed " + victim.getName() + "! Your level is now: " + killerLevel);
            
            saveData();
            applyEffects(killer);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        
        if (item != null && item.getType() == Material.NETHER_STAR) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§6Level Item")) {
                e.setCancelled(true);
                
                UUID uuid = p.getUniqueId();
                int level = dataConfig.getInt(uuid + ".level");
                level++;
                dataConfig.set(uuid + ".level", level);
                
                item.setAmount(item.getAmount() - 1);
                p.sendMessage("§aYou used a Level Item! Your level is now: " + level);
                
                saveData();
                applyEffects(p);
            }
        }
    }
    
    private ItemStack createLevelItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Level Item");
        meta.setLore(Arrays.asList("§7Right-click to gain a level!"));
        item.setItemMeta(meta);
        return item;
    }
    
    private void applyEffects(Player p) {
        UUID uuid = p.getUniqueId();
        int level = dataConfig.getInt(uuid + ".level");
        
        // Clear all effects first
        p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        p.removePotionEffect(PotionEffectType.SLOWNESS);
        p.removePotionEffect(PotionEffectType.HUNGER);
        p.removePotionEffect(PotionEffectType.GLOWING);
        p.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        p.removePotionEffect(PotionEffectType.LUCK);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.removePotionEffect(PotionEffectType.STRENGTH);
        
        // Apply effects based on level (good effects stack, bad ones don't)
        if (level >= 1) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 3) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
        }
        // Level 5 = nothing
        
        // Good effects that stack
        int goodLevels = Math.max(0, level - 5);
        
        if (level >= 6) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, Math.min(goodLevels - 1, 4), true, false));
        }
        if (level >= 7) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, Math.min(goodLevels - 1, 4) + 1, true, false));
        }
        if (level >= 8) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Math.min(goodLevels - 1, 4), true, false));
        }
        if (level >= 9) {
            if (invisibilityToggle.getOrDefault(uuid, true)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
            }
        }
        if (level >= 10) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, Math.min(goodLevels - 1, 4), true, false));
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();
        
        if (cmd.getName().equalsIgnoreCase("level")) {
            int level = dataConfig.getInt(uuid + ".level");
            int lives = dataConfig.getInt(uuid + ".lives");
            p.sendMessage("§6=== Your Stats ===");
            p.sendMessage("§aLevel: " + level);
            p.sendMessage("§cLives: " + lives);
            return true;
        }
        
        if (cmd.getName().equalsIgnoreCase("invis")) {
            int level = dataConfig.getInt(uuid + ".level");
            if (level < 9) {
                p.sendMessage("§cYou need to be level 9 or higher to use this command!");
                return true;
            }
            
            boolean current = invisibilityToggle.getOrDefault(uuid, true);
            invisibilityToggle.put(uuid, !current);
            
            if (!current) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
                p.sendMessage("§aInvisibility enabled!");
            } else {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.sendMessage("§cInvisibility disabled!");
            }
            return true;
        }
        
        if (cmd.getName().equalsIgnoreCase("unban")) {
            if (args.length == 0) {
                p.sendMessage("§cUsage: /unban <player>");
                return true;
            }
            
            String targetName = args[0];
            UUID targetUUID = null;
            
            // Find player UUID
            for (String key : dataConfig.getKeys(false)) {
                try {
                    UUID checkUUID = UUID.fromString(key);
                    if (Bukkit.getOfflinePlayer(checkUUID).getName().equalsIgnoreCase(targetName)) {
                        targetUUID = checkUUID;
                        break;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
            
            if (targetUUID == null) {
                p.sendMessage("§cPlayer not found!");
                return true;
            }
            
            if (!dataConfig.getBoolean(targetUUID + ".banned")) {
                p.sendMessage("§cThat player is not banned!");
                return true;
            }
            
            // Check for unban item in inventory
            boolean hasUnbanItem = false;
            for (ItemStack item : p.getInventory().getContents()) {
                if (item != null && item.getType() == Material.NETHER_STAR) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§5Unban Item")) {
                        item.setAmount(item.getAmount() - 1);
                        hasUnbanItem = true;
                        break;
                    }
                }
            }
            
            if (!hasUnbanItem) {
                p.sendMessage("§cYou need an Unban Item to unban players!");
                return true;
            }
            
            dataConfig.set(targetUUID + ".banned", false);
            dataConfig.set(targetUUID + ".lives", 5);
            saveData();
            
            p.sendMessage("§aYou have unbanned " + targetName + "!");
            Bukkit.broadcastMessage("§6" + targetName + " has been unbanned by " + p.getName() + "!");
            
            return true;
        }
        
        return false;
    }
    
    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}