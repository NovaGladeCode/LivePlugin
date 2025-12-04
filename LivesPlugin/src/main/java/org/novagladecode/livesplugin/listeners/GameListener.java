package org.novagladecode.livesplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.EffectManager;
import org.novagladecode.livesplugin.logic.ItemManager;

import java.util.UUID;

public class GameListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerDataManager dataManager;
    private final ItemManager itemManager;
    private final EffectManager effectManager;

    public GameListener(JavaPlugin plugin, PlayerDataManager dataManager, ItemManager itemManager,
            EffectManager effectManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.itemManager = itemManager;
        this.effectManager = effectManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        dataManager.initializePlayer(p.getUniqueId());

        // Check if banned
        if (dataManager.isBanned(p.getUniqueId())) {
            p.kickPlayer("§cYou are banned! Someone must craft an Unban Item to revive you.");
        } else {
            int level = dataManager.getLevel(p.getUniqueId());
            boolean invis = dataManager.isInvisibilityEnabled(p.getUniqueId());
            effectManager.applyEffects(p, level, invis);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        UUID victimUUID = victim.getUniqueId();
        int lives = dataManager.getLives(victimUUID);
        int level = dataManager.getLevel(victimUUID);

        // Drop level item if they have any levels
        if (level > 0) {
            ItemStack levelItem = itemManager.createLevelItem();
            victim.getWorld().dropItemNaturally(victim.getLocation(), levelItem);

            // Decrease level
            level--;
            dataManager.setLevel(victimUUID, level);
        }

        // Decrease lives
        lives--;
        dataManager.setLives(victimUUID, lives);

        if (lives <= 0) {
            dataManager.setBanned(victimUUID, true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                victim.kickPlayer("§cYou have lost all your lives! You are banned until someone crafts an Unban Item.");
            }, 5L);
        }

        dataManager.saveData();
        victim.sendMessage("§cYou died! Lives remaining: " + lives);

        // Give killer a level item and increase their level
        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            int killerLevel = dataManager.getLevel(killerUUID);
            killerLevel++;
            dataManager.setLevel(killerUUID, killerLevel);

            ItemStack levelItem = itemManager.createLevelItem();
            killer.getInventory().addItem(levelItem);
            killer.sendMessage("§aYou killed " + victim.getName() + "! Your level is now: " + killerLevel);

            dataManager.saveData();

            boolean invis = dataManager.isInvisibilityEnabled(killerUUID);
            effectManager.applyEffects(killer, killerLevel, invis);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (itemManager.isLevelItem(item)) {
            e.setCancelled(true);

            UUID uuid = p.getUniqueId();
            int level = dataManager.getLevel(uuid);
            level++;
            dataManager.setLevel(uuid, level);

            item.setAmount(item.getAmount() - 1);
            p.sendMessage("§aYou used a Level Item! Your level is now: " + level);

            dataManager.saveData();

            boolean invis = dataManager.isInvisibilityEnabled(uuid);
            effectManager.applyEffects(p, level, invis);
        }
    }
}
