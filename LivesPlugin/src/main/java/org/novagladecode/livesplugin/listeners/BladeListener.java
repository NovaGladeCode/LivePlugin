package org.novagladecode.livesplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.logic.ItemManager;

public class BladeListener implements Listener {

    private final ItemManager itemManager;
    private final LivePlugin plugin;

    public BladeListener(LivePlugin plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getItemManager();

        // Start a repeating task for passives
        Bukkit.getScheduler().runTaskTimer(plugin, this::handlePassives, 0L, 20L); // Every second
    }

    private void handlePassives() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack item = p.getInventory().getItemInMainHand();

            // Mistblade: Dolphin's Grace
            if (itemManager.isMistblade(item) && plugin.isAbilityEnabled("mistblade")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false, true));
            }

            // Soulblade: Strength I
            if (itemManager.isSoulblade(item) && plugin.isAbilityEnabled("soulblade")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, false, false, true));
            }

            // Ghostblade: Invisibility
            if (itemManager.isGhostblade(item) && plugin.isAbilityEnabled("ghostblade")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false, true));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.NETHERITE_SWORD)
            return;

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
            return;

        boolean isShift = p.isSneaking();
        String cmdArg = isShift ? "2" : "1";

        if (itemManager.isGhostblade(item)) {
            if (plugin.isAbilityEnabled("ghostblade"))
                p.performCommand("ghostblade " + cmdArg);
        } else if (itemManager.isDragonblade(item)) {
            if (plugin.isAbilityEnabled("dragonblade"))
                p.performCommand("dragonblade " + cmdArg);
        } else if (itemManager.isMistblade(item)) {
            if (plugin.isAbilityEnabled("mistblade"))
                p.performCommand("mistblade " + cmdArg);
        } else if (itemManager.isSoulblade(item)) {
            if (plugin.isAbilityEnabled("soulblade"))
                p.performCommand("soulblade " + cmdArg);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (itemManager.isDragonblade(p.getInventory().getItemInMainHand())
                        && plugin.isAbilityEnabled("dragonblade")) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
