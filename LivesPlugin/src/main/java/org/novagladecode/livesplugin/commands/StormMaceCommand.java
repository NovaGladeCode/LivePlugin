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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StormMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public StormMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
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
                || !item.getItemMeta().getDisplayName().equals("§e§lStorm Mace")) {
            p.sendMessage("§cYou must hold the Storm Mace to use this command!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /stormmace <1|2>");
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

        long currentTime = System.currentTimeMillis();
        if (cooldown1.containsKey(p.getUniqueId())) {
            long cooldown = cooldown1.get(p.getUniqueId());
            if (currentTime < cooldown) {
                p.sendMessage("§eThunder Strike is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateThunderStrike(p);
        cooldown1.put(p.getUniqueId(), currentTime + 180000); // 3 minutes
        p.sendMessage("§e§lThunder Strike activated!");
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
                p.sendMessage("§eStorm Surge is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateStormSurge(p);
        cooldown2.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
        p.sendMessage("§e§lStorm Surge activated!");
    }

    // ── Ability 1: Thunder Strike ──────────────────────────────────────────────
    // Strikes the nearest enemy with lightning then chains to 2 more targets.
    private void activateThunderStrike(Player p) {
        List<LivingEntity> nearby = getNearbyEnemies(p, 20);
        if (nearby.isEmpty()) {
            p.sendMessage("§cNo enemies nearby for Thunder Strike!");
            return;
        }

        // Build chain: primary target + up to 2 secondary targets
        List<LivingEntity> chain = new ArrayList<>();
        chain.add(nearby.get(0));
        for (int i = 1; i < nearby.size() && chain.size() < 3; i++) {
            // Only chain to secondary targets within 8 blocks of the primary
            if (nearby.get(i).getLocation().distance(chain.get(0).getLocation()) <= 8) {
                chain.add(nearby.get(i));
            }
        }

        // Play initial thunder boom at player
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.FLASH, p.getLocation().add(0, 1, 0), 1);

        for (int i = 0; i < chain.size(); i++) {
            final LivingEntity target = chain.get(i);
            final int chainIndex = i;
            final double damage = (chainIndex == 0) ? 18.0 : (chainIndex == 1) ? 12.0 : 7.0;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!target.isValid() || target.isDead()) return;

                Location tLoc = target.getLocation();

                // Visual lightning bolt (cosmetic, no fire)
                target.getWorld().strikeLightningEffect(tLoc);
                target.damage(damage, p);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));

                // Electric burst particles at target
                target.getWorld().spawnParticle(Particle.FIREWORK, tLoc.clone().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.2);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tLoc.clone().add(0, 1, 0), 50, 0.4, 0.8, 0.4, 0.05);
                target.getWorld().playSound(tLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.2f + chainIndex * 0.2f);

                if (chainIndex > 0) {
                    // Arc line between previous and current target
                    Location prev = chain.get(chainIndex - 1).getLocation().add(0, 1, 0);
                    Location curr = tLoc.clone().add(0, 1, 0);
                    double dist = prev.distance(curr);
                    int steps = (int) (dist * 3);
                    for (int s = 0; s <= steps; s++) {
                        double t = (double) s / steps;
                        Location arcLoc = prev.clone().add(
                            curr.getX() - prev.getX(), 0, curr.getZ() - prev.getZ()
                        ).multiply(t);
                        // Lerp manually
                        double lx = prev.getX() + (curr.getX() - prev.getX()) * t;
                        double ly = prev.getY() + (curr.getY() - prev.getY()) * t + Math.sin(t * Math.PI) * 2;
                        double lz = prev.getZ() + (curr.getZ() - prev.getZ()) * t;
                        Location arcPoint = new Location(tLoc.getWorld(), lx, ly, lz);
                        tLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, arcPoint, 2, 0.05, 0.05, 0.05, 0);
                    }
                }
            }, i * 5L); // 0, 5, 10 ticks — quick chain
        }
    }

    // ── Ability 2: Storm Surge ─────────────────────────────────────────────────
    // Unleashes a 6-second storm: repeated lightning strikes on all nearby enemies.
    private void activateStormSurge(Player p) {
        p.sendMessage("§6§l⚡ STORM SURGE UNLEASHED! ⚡");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);

        // Dramatic startup burst at player
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation().add(0, 1, 0), 100, 1, 1, 1, 0.15);
        p.getWorld().spawnParticle(Particle.FLASH, p.getLocation().add(0, 1, 0), 1);

        new BukkitRunnable() {
            int wave = 0;
            final int MAX_WAVES = 12; // 12 waves over ~6 seconds (every 10 ticks)

            @Override
            public void run() {
                if (wave >= MAX_WAVES || !p.isOnline()) {
                    // Final boom
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.6f);
                    p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation().add(0, 1, 0), 200, 2, 2, 2, 0.2);
                    p.getWorld().spawnParticle(Particle.EXPLOSION, p.getLocation().add(0, 1, 0), 5, 1, 1, 1, 0.1);
                    p.sendMessage("§eStorm Surge faded.");
                    this.cancel();
                    return;
                }

                List<LivingEntity> targets = getNearbyEnemies(p, 15);

                for (LivingEntity target : targets) {
                    if (!target.isValid() || target.isDead()) continue;
                    Location tLoc = target.getLocation();

                    // Alternate between direct strikes and arcing bolts for variety
                    if (wave % 2 == 0) {
                        target.getWorld().strikeLightningEffect(tLoc);
                    }
                    target.damage(6.0, p);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0));

                    // Electric sparks on the target
                    target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tLoc.clone().add(0, 1, 0), 15, 0.3, 0.6, 0.3, 0.05);
                    target.getWorld().playSound(tLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.6f, 1.0f + (wave % 4) * 0.1f);
                }

                // Ambient storm ring around player
                for (int deg = 0; deg < 360; deg += 20) {
                    double rad = Math.toRadians(deg + wave * 15);
                    double radius = 3 + Math.sin(wave * 0.5) * 1.5;
                    double x = Math.cos(rad) * radius;
                    double z = Math.sin(rad) * radius;
                    Location ringLoc = p.getLocation().clone().add(x, 0.5, z);
                    p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, ringLoc, 2, 0.1, 0.2, 0.1, 0.02);
                    if (wave % 3 == 0) {
                        p.getWorld().spawnParticle(Particle.FIREWORK, ringLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                wave++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────
    private List<LivingEntity> getNearbyEnemies(Player p, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity && e != p) {
                if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId())) continue;
                result.add((LivingEntity) e);
            }
        }
        // Sort by distance ascending (primary target = closest)
        result.sort((a, b) -> Double.compare(
                a.getLocation().distance(p.getLocation()),
                b.getLocation().distance(p.getLocation())));
        return result;
    }
}
