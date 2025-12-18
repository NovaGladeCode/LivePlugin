package org.novagladecode.livesplugin.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DragonbladeCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public DragonbladeCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getItemManager().isDragonblade(item)) {
            p.sendMessage("§cYou must be holding the Dragonblade!");
            return true;
        }

        if (!plugin.isAbilityEnabled("dragonblade")) {
            p.sendMessage("§cDragonblade abilities are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /dragonblade <1|2>");
            return true;
        }

        if (args[0].equals("1")) {
            useAbility1(p);
        } else if (args[0].equals("2")) {
            useAbility2(p);
        }

        return true;
    }

    public void useAbility1(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 5) {
            p.sendMessage("§cYou need Forge Level 5 to use this! (Current: " + points + "/5)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown1.getOrDefault(p.getUniqueId(), 0L) > now) {
            long remaining = (cooldown1.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage("§cDragon Leap is on cooldown! (" + remaining + "s)");
            return;
        }

        // Leap forward
        Vector dir = p.getLocation().getDirection().multiply(2.0).setY(1.0);
        p.setVelocity(dir);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        p.sendMessage("§6Dragon Leap!");

        // More particles
        p.getWorld().spawnParticle(org.bukkit.Particle.FLAME, p.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
        p.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, p.getLocation(), 30, 0.5, 0.5, 0.5, 0.05);

        cooldown1.put(p.getUniqueId(), now + 10000); // 10s
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need Forge Level 10 to use this! (Current: " + points + "/10)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown2.getOrDefault(p.getUniqueId(), 0L) > now) {
            long remaining = (cooldown2.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage("§cDragon Strike is on cooldown! (" + remaining + "s)");
            return;
        }

        // Dragon Strike: Explosion of fire and ice
        for (Entity e : p.getNearbyEntities(7, 7, 7)) {
            if (e instanceof LivingEntity && e != p) {
                if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                    continue;
                LivingEntity target = (LivingEntity) e;
                target.damage(10.0, p);
                target.setFireTicks(60);
                target.setFreezeTicks(100);
                target.sendMessage("§c§lYou have been struck by the Dragonblade's Fury!");

                // Target particles
                target.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, target.getLocation().add(0, 1, 0),
                        40, 0.5, 0.5, 0.5, 0.1);
                target.getWorld().spawnParticle(org.bukkit.Particle.FLAME, target.getLocation().add(0, 1, 0), 30, 0.4,
                        0.4, 0.4, 0.05);
            }
        }
        p.sendMessage("§6§lDragon Strike!");
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);

        // Large blast of particles from player
        p.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, p.getLocation().add(0, 1, 0), 150, 4, 1, 4, 0.1);
        p.getWorld().spawnParticle(org.bukkit.Particle.FLAME, p.getLocation().add(0, 1, 0), 100, 3, 1, 3, 0.05);

        cooldown2.put(p.getUniqueId(), now + 30000); // 30s
    }
}
