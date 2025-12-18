package org.novagladecode.livesplugin.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostbladeCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public GhostbladeCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getItemManager().isGhostblade(item)) {
            p.sendMessage("§cYou must be holding the Ghostblade!");
            return true;
        }

        if (!plugin.isAbilityEnabled("ghostblade")) {
            p.sendMessage("§cGhostblade abilities are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /ghostblade <1|2>");
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
            p.sendMessage("§cHaunt is on cooldown! (" + remaining + "s)");
            return;
        }

        // Haunt: Nausea + Blindness to nearby
        for (Entity e : p.getNearbyEntities(8, 8, 8)) {
            if (e instanceof org.bukkit.entity.LivingEntity && e != p) {
                org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) e;
                if (target instanceof Player && dataManager.isTrusted(p.getUniqueId(), target.getUniqueId()))
                    continue;
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                target.sendMessage("§7§oYou feel a ghostly presence...");
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.0f, 0.5f);

                target.getWorld().spawnParticle(org.bukkit.Particle.SOUL, target.getLocation().add(0, 1, 0), 30, 0.5,
                        0.5, 0.5, 0.02);
            }
        }
        p.getWorld().spawnParticle(org.bukkit.Particle.SOUL, p.getLocation().add(0, 1, 0), 100, 2, 2, 2, 0.05);
        p.sendMessage("§7You have haunted nearby souls.");
        p.playSound(p.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0f, 0.5f);
        cooldown1.put(p.getUniqueId(), now + 30000); // 30s
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
            p.sendMessage("§cSpectral Pull is on cooldown! (" + remaining + "s)");
            return;
        }

        // Spectral Pull: Pull enemies toward player and damage them
        for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (e instanceof org.bukkit.entity.LivingEntity && e != p) {
                org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) e;
                if (target instanceof Player && dataManager.isTrusted(p.getUniqueId(), target.getUniqueId()))
                    continue;

                // Damage
                target.damage(6.0, p);
                target.sendMessage("§7§lYou are being pulled into the spirit realm!");
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.5f);

                // Pull logic
                org.bukkit.util.Vector pull = p.getLocation().toVector().subtract(target.getLocation().toVector())
                        .normalize().multiply(1.2).setY(0.4);
                target.setVelocity(pull);

                // Particle Tether
                org.bukkit.Location start = p.getLocation().add(0, 1, 0);
                org.bukkit.Location end = target.getLocation().add(0, 1, 0);
                org.bukkit.util.Vector vector = end.toVector().subtract(start.toVector());
                double distance = start.distance(end);
                for (double i = 0; i <= distance; i += 0.5) {
                    org.bukkit.util.Vector current = vector.clone().multiply(i / distance);
                    start.getWorld().spawnParticle(org.bukkit.Particle.SOUL, start.clone().add(current), 1, 0.02, 0.02,
                            0.02, 0.01);
                }

                // Target particles
                target.getWorld().spawnParticle(org.bukkit.Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.3,
                        0.3, 0.3, 0.05);
            }
        }

        p.sendMessage("§7§lSpectral Pull!");
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
        p.getWorld().spawnParticle(org.bukkit.Particle.SOUL, p.getLocation().add(0, 1, 0), 100, 2, 1, 2, 0.1);
        cooldown2.put(p.getUniqueId(), now + 30000); // 30s
    }
}
