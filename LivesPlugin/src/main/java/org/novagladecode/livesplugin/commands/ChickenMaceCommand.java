package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class ChickenMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();

    public ChickenMaceCommand(JavaPlugin plugin) {
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
                || !item.getItemMeta().getDisplayName().equals("§eChicken Mace")) {
            p.sendMessage("§cYou must hold the Chicken Mace to use this command!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /chickenmace <1>");
            return true;
        }

        long currentTime = System.currentTimeMillis();

        if (args[0].equals("1")) {
            // Ability 1: Chicken Army
            if (cooldown1.containsKey(p.getUniqueId())) {
                long cooldown = cooldown1.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cChicken Army is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateChickenArmy(p);
            cooldown1.put(p.getUniqueId(), currentTime + 180000); // 3 minutes
            p.sendMessage("§eChicken Army activated!");
        } else {
            p.sendMessage("§cUsage: /chickenmace <1>");
        }

        return true;
    }

    private void activateChickenArmy(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 2.0f, 0.8f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0f, 1.0f);

        Location center = p.getLocation();

        // Spawn 15 angry chickens in a circle
        for (int i = 0; i < 15; i++) {
            double angle = (i / 15.0) * Math.PI * 2;
            double x = Math.cos(angle) * 3;
            double z = Math.sin(angle) * 3;

            Location spawnLoc = center.clone().add(x, 0.5, z);

            // Spawn visual effect
            spawnLoc.getWorld().spawnParticle(Particle.EXPLOSION, spawnLoc, 3);
            spawnLoc.getWorld().spawnParticle(Particle.CLOUD, spawnLoc, 10, 0.3, 0.3, 0.3, 0.05);

            // Spawn angry chicken
            Chicken chicken = spawnLoc.getWorld().spawn(spawnLoc, Chicken.class);
            chicken.setCustomName("§c§lANGRY CHICKEN");
            chicken.setCustomNameVisible(true);
            chicken.setAdult();

            // Make chicken stronger and faster
            if (chicken.getAttribute(Attribute.MAX_HEALTH) != null) {
                chicken.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
                chicken.setHealth(20.0);
            }
            if (chicken.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                chicken.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
            }

            // Add effects
            chicken.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            chicken.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));

            // Make chicken target nearby players (except the owner)
            final int chickenIndex = i;
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!chicken.isValid() || chicken.isDead()) {
                    task.cancel();
                    return;
                }

                // Find nearest player (except owner) within 20 blocks
                Player target = null;
                double nearestDistance = 20.0;

                for (Entity e : chicken.getNearbyEntities(20, 20, 20)) {
                    if (e instanceof Player && e != p) {
                        double dist = e.getLocation().distance(chicken.getLocation());
                        if (dist < nearestDistance) {
                            target = (Player) e;
                            nearestDistance = dist;
                        }
                    }
                }

                // Attack the target
                if (target != null) {
                    chicken.setTarget(target);

                    // Deal damage if close enough
                    if (nearestDistance < 2) {
                        target.damage(4.0, chicken);
                        chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 0.8f);

                        // Particle effect on hit
                        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.3, 0.5,
                                0.3, 0.1);
                    }
                }

                // Chicken particles
                if (chickenIndex % 3 == 0) {
                    chicken.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, chicken.getLocation().add(0, 1, 0), 1);
                }

            }, 0L, 10L); // Check every 10 ticks

            // Remove chicken after 30 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (chicken.isValid()) {
                    chicken.getWorld().spawnParticle(Particle.POOF, chicken.getLocation(), 20, 0.3, 0.3, 0.3, 0.1);
                    chicken.remove();
                }
            }, 600L); // 30 seconds
        }

        p.sendMessage("§e15 angry chickens have been summoned!");
    }
}
