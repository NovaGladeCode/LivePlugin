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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

import java.util.HashMap;
import java.util.UUID;

public class NetherMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public NetherMaceCommand(JavaPlugin plugin) {
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
                || !item.getItemMeta().getDisplayName().equals("§cNether Mace")) {
            p.sendMessage("§cYou must hold the Nether Mace to use this command!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /nethermace <1|2>");
            return true;
        }

        long currentTime = System.currentTimeMillis();

        if (args[0].equals("1")) {
            // Ability 1: Infernal Wrath
            if (cooldown1.containsKey(p.getUniqueId())) {
                long cooldown = cooldown1.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cInfernal Wrath is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateInfernalWrath(p);
            cooldown1.put(p.getUniqueId(), currentTime + 240000); // 4 minutes
            p.sendMessage("§6Infernal Wrath activated!");

        } else if (args[0].equals("2")) {
            // Ability 2: Fire Tornado
            if (cooldown2.containsKey(p.getUniqueId())) {
                long cooldown = cooldown2.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cFire Tornado is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateFireTornado(p);
            cooldown2.put(p.getUniqueId(), currentTime + 360000); // 6 minutes
            p.sendMessage("§6Fire Tornado activated!");
        } else {
            p.sendMessage("§cUsage: /nethermace <1|2>");
        }

        return true;
    }

    private void activateInfernalWrath(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 2.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_AMBIENT, 2.0f, 1.0f);

        Location center = p.getLocation();

        // Meteor shower over 6 seconds
        for (int wave = 0; wave < 12; wave++) {
            final int w = wave;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Spawn 5 meteors per wave
                for (int i = 0; i < 5; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double distance = Math.random() * 20;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    Location meteorStart = center.clone().add(x, 30, z);
                    Location meteorEnd = center.clone().add(x, 0, z);

                    createMeteor(meteorStart, meteorEnd, p);
                }

                // Play sound each wave
                p.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
            }, wave * 10L);
        }
    }

    private void createMeteor(Location start, Location end, Player p) {
        // Create falling meteor effect
        for (int i = 0; i < 20; i++) {
            final int step = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double progress = step / 20.0;
                Location current = start.clone().add(
                        (end.getX() - start.getX()) * progress,
                        (end.getY() - start.getY()) * progress,
                        (end.getZ() - start.getZ()) * progress);

                // Meteor particles
                current.getWorld().spawnParticle(Particle.FLAME, current, 20, 0.3, 0.3, 0.3, 0.1);
                current.getWorld().spawnParticle(Particle.LAVA, current, 5, 0.2, 0.2, 0.2, 0);
                current.getWorld().spawnParticle(Particle.SMOKE, current, 10, 0.3, 0.3, 0.3, 0.05);

                // Impact on last step
                if (step == 19) {
                    current.getWorld().spawnParticle(Particle.EXPLOSION, end, 3);
                    current.getWorld().playSound(end, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);

                    // Damage nearby entities
                    for (Entity e : end.getWorld().getNearbyEntities(end, 3, 3, 3)) {
                        if (e instanceof LivingEntity && e != p) {
                            LivingEntity le = (LivingEntity) e;
                            le.damage(8.0, p);
                            le.setFireTicks(60); // 3 seconds of fire

                            // Knockback
                            Vector knockback = e.getLocation().toVector().subtract(end.toVector()).normalize();
                            knockback.setY(0.5);
                            e.setVelocity(knockback.multiply(0.8));
                        }
                    }

                    // Create temporary lava effect (particles only, not actual lava)
                    for (int lavaTime = 0; lavaTime < 40; lavaTime++) {
                        final int t = lavaTime;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            for (int deg = 0; deg < 360; deg += 30) {
                                double rad = Math.toRadians(deg);
                                double lx = Math.cos(rad) * 2;
                                double lz = Math.sin(rad) * 2;
                                Location lavaLoc = end.clone().add(lx, 0.1, lz);
                                lavaLoc.getWorld().spawnParticle(Particle.DRIPPING_LAVA, lavaLoc, 2, 0.1, 0, 0.1, 0);
                            }
                        }, t * 2L);
                    }
                }
            }, i);
        }
    }

    private void activateFireTornado(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_POP, 2.0f, 1.0f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.5f, 1.5f);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 2.0f, 1.0f);

        // Fire tornado follows player for 10 seconds (200 ticks) - reduced from 15s
        final int[] tick = { 0 };

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            tick[0]++;

            if (tick[0] > 200 || !p.isOnline()) { // Changed from 300 to 200 ticks
                task.cancel();
                p.sendMessage("§6Fire Tornado dissipated!");
                return;
            }

            Location center = p.getLocation();

            // Create spinning tornado effect with MORE particles
            double height = 10;
            for (double y = 0; y < height; y += 0.3) { // Changed from 0.5 to 0.3 for more layers
                double radius = 8 - (y / height * 3); // Narrows at top
                double angle = (tick[0] * 15 + y * 20) % 360;
                double rad = Math.toRadians(angle);

                double x = Math.cos(rad) * radius;
                double z = Math.sin(rad) * radius;

                Location particleLoc = center.clone().add(x, y, z);
                // Increased particle counts significantly
                particleLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc, 8, 0.15, 0.15, 0.15, 0.03);
                particleLoc.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 5, 0.15, 0.15, 0.15, 0.02);
                particleLoc.getWorld().spawnParticle(Particle.LAVA, particleLoc, 2, 0.1, 0.1, 0.1, 0);

                // Add lava drips more frequently
                if (Math.random() < 0.3) { // Changed from 0.1 to 0.3
                    particleLoc.getWorld().spawnParticle(Particle.DRIPPING_LAVA, particleLoc, 2);
                }

                // Add additional fire particles for density
                if (Math.random() < 0.4) {
                    particleLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 3, 0.1, 0.1, 0.1, 0.01);
                }
            }

            // Damage entities within tornado radius - REDUCED damage
            for (Entity e : center.getWorld().getNearbyEntities(center, 8, 10, 8)) {
                if (e instanceof LivingEntity && e != p) {
                    LivingEntity le = (LivingEntity) e;

                    // Check if within tornado radius
                    double distance = e.getLocation().distance(center);
                    if (distance <= 8) {
                        le.damage(1.0, p); // Reduced from 2.0 to 1.0
                        le.setFireTicks(40); // 2 seconds of fire

                        // Pull toward center slightly
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize();
                        pull.setY(0.2);
                        e.setVelocity(pull.multiply(0.3));
                    }
                }
            }

            // Play crackling sound periodically
            if (tick[0] % 20 == 0) {
                p.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.0f);
            }

        }, 0L, 2L); // Run every 2 ticks for smooth animation
    }
}
