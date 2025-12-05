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
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();

    public ChickenMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
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

            spawnLoc.getWorld().spawnParticle(Particle.EXPLOSION, spawnLoc, 3);
            spawnLoc.getWorld().spawnParticle(Particle.CLOUD, spawnLoc, 10, 0.3, 0.3, 0.3, 0.05);

            Chicken chicken = spawnLoc.getWorld().spawn(spawnLoc, Chicken.class);
            chicken.setCustomName("§c§lANGRY CHICKEN");
            chicken.setCustomNameVisible(true);
            chicken.setAdult();

            if (chicken.getAttribute(Attribute.MAX_HEALTH) != null) {
                chicken.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
                chicken.setHealth(20.0);
            }
            if (chicken.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                chicken.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
            }

            chicken.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
            chicken.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));

            final int chickenIndex = i;
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!chicken.isValid() || chicken.isDead()) {
                    task.cancel();
                    return;
                }

                // Find nearest TRUSTED player check
                Player target = null;
                double nearestDistance = 20.0;

                for (Entity e : chicken.getNearbyEntities(20, 20, 20)) {
                    if (e instanceof Player && e != p) {
                        Player candidate = (Player) e;
                        // Avoid trusting players
                        if (dataManager.isTrusted(p.getUniqueId(), candidate.getUniqueId())) {
                            continue;
                        }

                        double dist = e.getLocation().distance(chicken.getLocation());
                        if (dist < nearestDistance) {
                            target = candidate;
                            nearestDistance = dist;
                        }
                    }
                }

                if (target != null) {
                    chicken.setTarget(target);

                    // Improved Tracking / Velocity boost
                    if (nearestDistance > 1.5) {
                        org.bukkit.util.Vector direction = target.getLocation().toVector()
                                .subtract(chicken.getLocation().toVector()).normalize();
                        // Give a small push towards target
                        chicken.setVelocity(direction.multiply(0.4).setY(0.2));
                        // Note: Only jump (Y) if on ground? For chaos, small hop is fine.
                    }

                    if (nearestDistance < 2) {
                        target.damage(4.0, chicken);
                        chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 0.8f);
                        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.3, 0.5,
                                0.3, 0.1);
                    }
                }

                if (chickenIndex % 3 == 0) {
                    chicken.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, chicken.getLocation().add(0, 1, 0), 1);
                }

            }, 0L, 10L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (chicken.isValid()) {
                    chicken.getWorld().spawnParticle(Particle.POOF, chicken.getLocation(), 20, 0.3, 0.3, 0.3, 0.1);
                    chicken.remove();
                }
            }, 600L);
        }

        p.sendMessage("§e15 angry chickens have been summoned! They will hunt untrusted players.");
    }
}
