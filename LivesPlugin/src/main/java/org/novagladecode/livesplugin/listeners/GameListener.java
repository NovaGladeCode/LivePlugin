package org.novagladecode.livesplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.gui.UnbanGUI;
import org.novagladecode.livesplugin.logic.EffectManager;
import org.novagladecode.livesplugin.logic.ItemManager;

import java.util.UUID;

public class GameListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerDataManager dataManager;
    private final ItemManager itemManager;
    private final EffectManager effectManager;
    private final UnbanGUI unbanGUI;

    public GameListener(JavaPlugin plugin, PlayerDataManager dataManager, ItemManager itemManager,
            EffectManager effectManager, UnbanGUI unbanGUI) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.itemManager = itemManager;
        this.effectManager = effectManager;
        this.unbanGUI = unbanGUI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        dataManager.initializePlayer(p.getUniqueId());

        // Check if banned
        if (dataManager.isBanned(p.getUniqueId())) {
            p.kickPlayer("§cYou are banned! Someone must craft an Unban Token to revive you.");
        } else {
            int level = dataManager.getLevel(p.getUniqueId());
            effectManager.applyEffects(p, level);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        UUID victimUUID = victim.getUniqueId();
        int level = dataManager.getLevel(victimUUID);

        // Victim loses one level on death (if they have any)
        if (level > 0) {
            level--;
            dataManager.setLevel(victimUUID, level);
        }

        if (level <= 0) {
            dataManager.setBanned(victimUUID, true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                victim.kickPlayer(
                        "§cYou have lost all your levels! You are banned until someone crafts an Unban Token.");
            }, 5L);
        }

        dataManager.saveData();
        victim.sendMessage("§cYou died! Levels remaining: " + level);

        // Give killer a level
        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            int killerLevel = dataManager.getLevel(killerUUID);
            if (killerLevel < 15) {
                killerLevel++;
            }
            dataManager.setLevel(killerUUID, killerLevel);

            killer.sendMessage("§aYou killed " + victim.getName() + "! Your level is now: " + killerLevel);

            dataManager.saveData();

            effectManager.applyEffects(killer, killerLevel);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null)
            return;

        // Check if right-clicking with Level Item
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && itemManager.isLevelItem(item)) {
            e.setCancelled(true);

            UUID uuid = p.getUniqueId();
            int currentLevel = dataManager.getLevel(uuid);

            if (currentLevel >= 15) {
                p.sendMessage("§cYou are already at max level (15)!");
                e.setCancelled(true);
                return;
            }

            dataManager.setLevel(uuid, currentLevel + 1);
            dataManager.saveData();

            p.sendMessage("§aYou used a Level Item! Your level is now: " + (currentLevel + 1));

            // Consume the item
            item.setAmount(item.getAmount() - 1);

            // Apply effects with new level
            effectManager.applyEffects(p, currentLevel + 1);
        }

        // Check if right-clicking with Unban Token
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && itemManager.isUnbanItem(item)) {
            e.setCancelled(true);
            unbanGUI.openUnbanMenu(p);
        }
    }

}
