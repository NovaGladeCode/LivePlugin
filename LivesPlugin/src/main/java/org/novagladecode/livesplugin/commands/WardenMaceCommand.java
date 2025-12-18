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
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public WardenMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
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

        long currentTime = System.currentTimeMillis();
        if (cooldown1.containsKey(p.getUniqueId())) {
            long cooldown = cooldown1.get(p.getUniqueId());
            if (currentTime < cooldown) {
                p.sendMessage("§cSonic Wave is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        // Ability 1: Sonic Wave (Radial Push)
        activateSonicWave(p);
        cooldown1.put(p.getUniqueId(), currentTime + 240000); // 4 minutes
        p.sendMessage("§bSonic Wave activated!");
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need Forge Level 10 to use this! (Current: " + points + "/10)");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldown2.containsKey(p.getUniqueId())) {
            long cooldown = cooldown2.get(p.getUniqueId());
            if (currentTime < cooldown) {
                p.sendMessage("§cWarden's Grasp is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateGrasp(p);
        cooldown2.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
        p.sendMessage("§bWarden's Grasp activated!");
    }

    private void activateSonicWave(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.5f, 0.5f);

        // Launch player upward
        p.setVelocity(new Vector(0, 1.2, 0));

        Location center = p.getLocation();

        // Delayed shockwave after brief upward launch
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location shockwaveCenter = center.clone();

            // Visual expanding wave with multiple particle types
            for (double r = 1; r <= 15; r += 0.3) {
                final double radius = r;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Create circular wave
                    for (int deg = 0; deg < 360; deg += 8) {
                        double rad = Math.toRadians(deg);
                        double x = Math.cos(rad) * radius;
                        double z = Math.sin(rad) * radius;
                        Location loc = shockwaveCenter.clone().add(x, 0.5, z);

                        // Multiple particle layers for dramatic effect
                        p.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1, 0, 0, 0, 0);
                        p.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 3, 0.2, 0.2, 0.2, 0.01);
                        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                    }

                    // Sound effect for wave expansion
                    if (radius % 3 == 0) {
                        p.getWorld().playSound(shockwaveCenter, Sound.ENTITY_WARDEN_HEARTBEAT, 0.8f, 1.5f);
                    }
                }, (long) (radius * 0.8));
            }

            // Push entities
            for (Entity e : p.getWorld().getNearbyEntities(shockwaveCenter, 15, 15, 15)) {
                if (e instanceof LivingEntity && e != p) {
                    if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                        continue;

                    LivingEntity le = (LivingEntity) e;
                    double distance = le.getLocation().distance(shockwaveCenter);

                    // Stronger knockback for closer entities
                    double knockbackMultiplier = 1 - (distance / 15);
                    Vector dir = le.getLocation().toVector().subtract(shockwaveCenter.toVector()).normalize();
                    dir.setY(0.8);
                    le.setVelocity(dir.multiply(3.5 * Math.max(0.3, knockbackMultiplier)));

                    le.damage(12.0, p);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                }
            }
        }, 10L);
    }

    private void activateGrasp(Player p) {
        // Find the closest non-trusted player within 20 blocks
        Player closest = null;
        double minDist = Double.MAX_VALUE;
        for (Entity e : p.getNearbyEntities(20, 20, 20)) {
            if (e instanceof Player && e != p) {
                Player test = (Player) e;
                if (dataManager.isTrusted(p.getUniqueId(), test.getUniqueId()))
                    continue;
                double d = test.getLocation().distance(p.getLocation());
                if (d < minDist) {
                    minDist = d;
                    closest = test;
                }
            }
        }
        if (closest == null) {
            p.sendMessage("§cNo valid players found nearby for Warden's Grasp!");
            return;
        }
        Location trapLoc = closest.getLocation();
        trapLoc.getWorld().playSound(trapLoc, Sound.ENTITY_WARDEN_EMERGE, 1.5f, 0.5f);
        trapLoc.getWorld().playSound(trapLoc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.5f, 0.6f);

        // Sculk visual for drama (client-only changed blocks)
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y <= 1; y++) {
                    Location bLoc = trapLoc.clone().add(x, y, z);
                    if (bLoc.distance(trapLoc) <= radius && bLoc.getBlock().getType().isSolid()) {
                        p.sendBlockChange(bLoc, Material.SCULK.createBlockData());
                        bLoc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, bLoc.clone().add(0, 1, 0), 1, 0.2, 0.2,
                                0.2, 0.05);
                    }
                }
            }
        }

        // Continuous tracking (2 seconds: 40 ticks)
        final Player closestFinal = closest;
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            final int MAX_TICKS = 40;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }
                // Only affects the closest target
                if (closestFinal.isOnline() && closestFinal.getWorld().equals(trapLoc.getWorld())) {
                    // Fangs/track effect at target
                    closestFinal.getWorld().spawn(closestFinal.getLocation(), org.bukkit.entity.EvokerFangs.class);
                    // Pull toward center (optional: slight force)
                    Vector pull = trapLoc.toVector().subtract(closestFinal.getLocation().toVector()).normalize()
                            .multiply(1.2);
                    closestFinal.setVelocity(pull);

                    // Grasp debuffs (reapply)
                    closestFinal.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));
                    closestFinal.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
                    closestFinal.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
                    closestFinal.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    closestFinal.damage(4.0, p); // Lighter damage since less time
                    closestFinal.getWorld().playSound(closestFinal.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT,
                            0.5f, 1.0f);
                    if (ticks == 0) {
                        closestFinal.sendMessage("§3§lTHE WARDEN TRACKS YOU!");
                    }
                }
                // Visual effects (big!)
                trapLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, trapLoc, 10, 3, 3, 3, 0.04);
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
