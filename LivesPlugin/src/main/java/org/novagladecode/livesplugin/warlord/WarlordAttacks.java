package org.novagladecode.livesplugin.warlord;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;

public final class WarlordAttacks {

    public static final String MINION_META = "warlord_minion";

    private WarlordAttacks() {}

    // --- Ground Slam ---
    public static void groundSlam(LivingEntity boss, List<Player> nearby) {
        Location loc = boss.getLocation();
        World w = boss.getWorld();
        w.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.5f);
        w.strikeLightningEffect(loc);
        // Ring of particles
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            Location pl = loc.clone().add(Math.cos(angle) * 6, 0.1, Math.sin(angle) * 6);
            w.spawnParticle(Particle.EXPLOSION, pl, 1, 0, 0, 0, 0);
            w.spawnParticle(Particle.LAVA, pl, 3, 0.2, 0.1, 0.2, 0);
        }
        for (Player p : nearby) {
            p.damage(12.0, boss);
            p.setVelocity(p.getLocation().toVector().subtract(loc.toVector())
                    .normalize().setY(0.6).multiply(1.5));
            p.sendMessage("§c§lGROUND SLAM!");
        }
    }

    // --- Dash Attack ---
    public static void dash(LivingEntity boss, Player target) {
        Location from = boss.getLocation();
        World w = boss.getWorld();
        Vector dir = target.getLocation().subtract(from).toVector().normalize();
        boss.setVelocity(dir.multiply(2.5).setY(0.4));
        w.playSound(from, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.5f, 1.2f);
        // Trail particles scheduled separately to avoid blocking
        Bukkit.getScheduler().runTaskLater(boss.getServer().getPluginManager()
                .getPlugin("ForgeboundSMP") != null
                ? boss.getServer().getPluginManager().getPlugin("ForgeboundSMP")
                : Bukkit.getPluginManager().getPlugins()[0], () -> {
            if (target.isOnline() && from.distanceSquared(boss.getLocation()) < 4) {
                target.damage(14.0, boss);
            }
        }, 10L);
        for (int i = 0; i < 20; i++) {
            w.spawnParticle(Particle.CRIT, from.clone().add(dir.clone().multiply(i * 0.3)), 2, 0.1, 0.1, 0.1, 0);
        }
    }

    // --- Dash (plugin-aware variant used from WarlordBoss via manager) ---
    public static void dash(JavaPlugin plugin, LivingEntity boss, Player target) {
        Location from = boss.getLocation();
        World w = boss.getWorld();
        Vector dir = target.getLocation().subtract(from).toVector().normalize();
        boss.setVelocity(dir.multiply(2.5).setY(0.4));
        w.playSound(from, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.5f, 1.2f);
        for (int i = 0; i < 20; i++) {
            Location trail = from.clone().add(dir.clone().multiply(i * 0.3));
            w.spawnParticle(Particle.CRIT, trail, 2, 0.1, 0.1, 0.1, 0);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (target.isOnline()) {
                double dist = boss.getLocation().distanceSquared(target.getLocation());
                if (dist < 16) target.damage(14.0, boss);
            }
        }, 10L);
    }

    // --- Summon Minions ---
    public static void summonMinions(JavaPlugin plugin, LivingEntity boss) {
        Location loc = boss.getLocation();
        World w = boss.getWorld();
        int count = 3;
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(i * (360.0 / count));
            Location spawn = loc.clone().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
            ZombifiedPiglin minion = (ZombifiedPiglin) w.spawnEntity(spawn, EntityType.ZOMBIFIED_PIGLIN);
            minion.setMetadata(MINION_META, new FixedMetadataValue(plugin, true));
            minion.setCustomName("§c⚔ Warlord Guard");
            minion.setCustomNameVisible(true);
            minion.setRemoveWhenFarAway(true);
            var hp = minion.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (hp != null) hp.setBaseValue(30);
            minion.setHealth(30);
            w.spawnParticle(Particle.PORTAL, spawn.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        }
        w.playSound(loc, Sound.ENTITY_WARDEN_EMERGE, 1f, 0.8f);
        for (Player p : w.getPlayers()) p.sendMessage("§6§lWarlord summons his guards!");
    }

    // --- Shockwave ---
    public static void shockwave(JavaPlugin plugin, LivingEntity boss, List<Player> nearby) {
        Location loc = boss.getLocation();
        World w = boss.getWorld();
        // Expanding ring animation
        for (int ring = 1; ring <= 12; ring++) {
            final int r = ring;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < 24; i++) {
                    double angle = Math.toRadians(i * 15);
                    Location pl = loc.clone().add(Math.cos(angle) * r, 0.2, Math.sin(angle) * r);
                    w.spawnParticle(Particle.SONIC_BOOM, pl, 1, 0, 0, 0, 0);
                }
            }, ring * 2L);
        }
        w.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2f, 0.7f);
        for (Player p : nearby) {
            double dist = p.getLocation().distanceSquared(loc);
            if (dist < 225) { // within 15 blocks
                double dmg = 10.0 * (1 - Math.sqrt(dist) / 15.0);
                p.damage(dmg, boss);
                Vector knock = p.getLocation().toVector().subtract(loc.toVector()).normalize().setY(0.4).multiply(2.0);
                p.setVelocity(knock);
            }
        }
    }

    // --- Fireball Barrage ---
    public static void fireballBarrage(JavaPlugin plugin, LivingEntity boss, Player target) {
        Location loc = boss.getLocation().add(0, 1.5, 0);
        World w = boss.getWorld();
        w.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.8f);
        for (int i = 0; i < 5; i++) {
            final int shot = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!target.isOnline()) return;
                Vector dir = target.getLocation().add(0, 1, 0).subtract(boss.getLocation().add(0, 1.5, 0)).toVector().normalize();
                dir.add(new Vector((Math.random() - 0.5) * 0.3, 0, (Math.random() - 0.5) * 0.3));
                Fireball fb = w.spawn(loc, Fireball.class);
                fb.setDirection(dir.multiply(1.5));
                fb.setShooter(boss);
                fb.setIsIncendiary(false);
                fb.setYield(1.5f);
            }, shot * 6L);
        }
    }

    // Overload without plugin (uses scheduler workaround)
    public static void fireballBarrage(LivingEntity boss, Player target) {
        // Fallback — minimal version
        World w = boss.getWorld();
        w.playSound(boss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.8f);
        Vector dir = target.getLocation().add(0, 1, 0).subtract(boss.getLocation().add(0, 1.5, 0)).toVector().normalize();
        Fireball fb = w.spawn(boss.getLocation().add(0, 1.5, 0), Fireball.class);
        fb.setDirection(dir.multiply(1.5));
        fb.setShooter(boss);
        fb.setIsIncendiary(false);
        fb.setYield(1.5f);
    }

    // --- Pull Players ---
    public static void pullPlayers(LivingEntity boss, List<Player> nearby) {
        Location loc = boss.getLocation().add(0, 1, 0);
        World w = boss.getWorld();
        w.playSound(loc, Sound.ENTITY_WARDEN_TENDRIL_CLICKS, 2f, 0.5f);
        w.spawnParticle(Particle.END_ROD, loc, 40, 2, 1, 2, 0.1);
        for (Player p : nearby) {
            Vector pull = loc.toVector().subtract(p.getLocation().add(0, 1, 0).toVector()).normalize().multiply(2.0).setY(0.3);
            p.setVelocity(pull);
            p.sendMessage("§5§lVOID PULL!");
            w.spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
        }
    }
}
