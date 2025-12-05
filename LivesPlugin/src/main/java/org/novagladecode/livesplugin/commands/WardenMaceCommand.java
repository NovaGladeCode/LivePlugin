package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class WardenMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public WardenMaceCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() != Material.MACE || !item.hasItemMeta()
                || !item.getItemMeta().getDisplayName().equals("§3Warden Mace")) {
            p.sendMessage("§cYou must hold the Warden Mace to use this command!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /wardenmace <1|2>");
            return true;
        }

        long currentTime = System.currentTimeMillis();

        if (args[0].equals("1")) {
            // Ability 1: Sculk Resonance
            if (cooldown1.containsKey(p.getUniqueId())) {
                long cooldown = cooldown1.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cSculk Resonance is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateResonance(p);
            cooldown1.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
            p.sendMessage("§3Sculk Resonance activated!");

        } else if (args[0].equals("2")) {
            // Ability 2: Sonic Beam
            if (cooldown2.containsKey(p.getUniqueId())) {
                long cooldown = cooldown2.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cSonic Beam is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateBeam(p);
            cooldown2.put(p.getUniqueId(), currentTime + 240000); // 4 minutes
            p.sendMessage("§bSonic Shockwave activated!");
        }

        return true;
    }

    private void activateResonance(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);

        // Visuals: Expanding circle
        for (int i = 0; i < 20; i++) {
            final int r = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int degree = 0; degree < 360; degree += 10) {
                    double radians = Math.toRadians(degree);
                    double x = Math.cos(radians) * r;
                    double z = Math.sin(radians) * r;
                    p.getWorld().spawnParticle(Particle.SCULK_SOUL, p.getLocation().add(x, 0.5, z), 1);
                    p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(x, 0.5, z), 1);
                }
            }, i * 2L);
        }

        // Effects
        for (Entity e : p.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity && e != p) {
                LivingEntity le = (LivingEntity) e;
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 160, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
            }
        }
    }

    private void activateBeam(Player p) {
        // Play freeze sounds
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.5f, 1.0f);

        Location playerLoc = p.getLocation();

        // Create freeze effect particles
        for (int i = 0; i < 10; i++) {
            final int radius = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int degree = 0; degree < 360; degree += 15) {
                    double radians = Math.toRadians(degree);
                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;
                    Location particleLoc = playerLoc.clone().add(x, 0.2, z);
                    p.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 3, 0.1, 0.3, 0.1, 0);
                    p.getWorld().spawnParticle(Particle.SCULK_CHARGE, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                }
            }, i * 1L);
        }

        // Freeze nearby players within 10 blocks for 3 seconds
        for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                // Apply extreme slowness and jump boost debuff to freeze them
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, false, true)); // 3 seconds
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 128, false, true)); // Negative
                                                                                                             // jump
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 10, false, true));

                // Stop their velocity
                target.setVelocity(new Vector(0, 0, 0));

                target.sendMessage("§3You've been frozen by the Warden Mace!");

                // Spawn freeze particles around the frozen player
                for (int i = 0; i < 10; i++) {
                    target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 5, 0.3, 0.5,
                            0.3, 0);
                }
            }
        }

        // Launch the user into the air
        Vector launchVelocity = new Vector(0, 1.5, 0); // Launch upward
        p.setVelocity(launchVelocity);
        p.sendMessage("§bYou've been launched into the air!");
    }
}
