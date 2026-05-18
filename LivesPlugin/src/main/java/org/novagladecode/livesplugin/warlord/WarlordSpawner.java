package org.novagladecode.livesplugin.warlord;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents one active Warlord Phase-2 spawner.
 * Entity: Slime (size 3, frozen), with an ArmorStand hologram above it.
 */
public class WarlordSpawner {

    public static final String META_KEY = "warlord_spawner";
    public static final String META_HP  = "spawner_hp";

    private final JavaPlugin   plugin;
    private final WarlordManager manager;
    private final UUID         id = UUID.randomUUID();

    private Slime       entity;
    private ArmorStand  hologram;

    private double hp    = WarlordConfig.SPAWNER_MAX_HEALTH;
    private boolean alive = true;
    private int     minionCount = 0;

    private BukkitRunnable spawnTask;
    private BukkitRunnable particleTask;

    public WarlordSpawner(JavaPlugin plugin, Location loc, WarlordManager manager) {
        this.plugin  = plugin;
        this.manager = manager;
        spawnEntities(loc);
        startSpawning();
        startParticles();
    }

    private void spawnEntities(Location loc) {
        World w = loc.getWorld();

        // Main spawner body — big Slime
        entity = (Slime) w.spawnEntity(loc, EntityType.SLIME);
        entity.setSize(4);
        entity.setAI(false);
        entity.setInvulnerable(true); // damage handled via event
        entity.setRemoveWhenFarAway(false);
        entity.setCustomName("§c§lWARLORD SPAWNER");
        entity.setCustomNameVisible(false);
        entity.setGlowing(true);
        entity.setMetadata(META_KEY, new FixedMetadataValue(plugin, id.toString()));
        entity.setMetadata(META_HP,  new FixedMetadataValue(plugin, hp));

        // Hologram above
        Location holoLoc = loc.clone().add(0, 3.5, 0);
        hologram = (ArmorStand) w.spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setInvulnerable(true);
        hologram.setSmall(true);
        hologram.setMarker(true);
        updateHologram();

        w.playSound(loc, Sound.ENTITY_WARDEN_EMERGE, 1.5f, 0.6f);
        w.spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 60, 1, 1, 1, 0.2);
    }

    private void updateHologram() {
        if (hologram == null || !hologram.isValid()) return;
        int pct = (int) ((hp / WarlordConfig.SPAWNER_MAX_HEALTH) * 100);
        hologram.setCustomName("§c§lWARLORD SPAWNER\n§7HP: §e" + (int) hp + " §7(" + pct + "%)");
        hologram.setCustomNameVisible(true);
    }

    private void startSpawning() {
        spawnTask = new BukkitRunnable() {
            @Override public void run() {
                if (!alive) { cancel(); return; }
                if (minionCount >= WarlordConfig.MAX_MINIONS_PER_SPAWNER) return;
                Location loc = entity.getLocation();
                World w = loc.getWorld();
                ZombifiedPiglin minion = (ZombifiedPiglin) w.spawnEntity(loc, EntityType.ZOMBIFIED_PIGLIN);
                minion.setMetadata(WarlordAttacks.MINION_META, new FixedMetadataValue(plugin, true));
                minion.setMetadata("spawner_id", new FixedMetadataValue(plugin, id.toString()));
                minion.setCustomName("§c⚔ Warlord Grunt");
                minion.setCustomNameVisible(true);
                minion.setRemoveWhenFarAway(true);
                var hp = minion.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (hp != null) hp.setBaseValue(20);
                minion.setHealth(20);
                minionCount++;
                w.spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                w.playSound(loc, Sound.ENTITY_ZOMBIE_PIGLIN_ANGRY, 1f, 0.8f);
            }
        };
        spawnTask.runTaskTimer(plugin, 40L, WarlordConfig.SPAWN_INTERVAL_TICKS);
    }

    private void startParticles() {
        particleTask = new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (!alive) { cancel(); return; }
                if (!entity.isValid()) { cancel(); return; }
                Location loc = entity.getLocation().add(0, 2, 0);
                World w = loc.getWorld();
                // Swirl
                double angle = Math.toRadians(tick * 15);
                for (int i = 0; i < 4; i++) {
                    double a = angle + Math.toRadians(i * 90);
                    Location pl = loc.clone().add(Math.cos(a) * 2, Math.sin(tick * 0.1) * 0.5, Math.sin(a) * 2);
                    w.spawnParticle(Particle.SOUL_FIRE_FLAME, pl, 1, 0, 0, 0, 0);
                }
                tick++;
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 3L);
    }

    /** Called when a player hits the spawner entity. Returns true if still alive. */
    public boolean damage(double amount) {
        if (!alive) return false;
        hp -= amount;
        entity.setMetadata(META_HP, new FixedMetadataValue(plugin, hp));
        updateHologram();
        entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
        if (hp <= 0) {
            destroy();
            return false;
        }
        return true;
    }

    public void destroy() {
        if (!alive) return;
        alive = false;
        if (spawnTask   != null) spawnTask.cancel();
        if (particleTask != null) particleTask.cancel();

        Location loc = entity.getLocation();
        World w = loc.getWorld();

        // Kill all minions from this spawner
        for (Entity e : w.getEntities()) {
            if (e.hasMetadata("spawner_id") &&
                    e.getMetadata("spawner_id").get(0).asString().equals(id.toString())) {
                w.spawnParticle(Particle.SOUL_FIRE_FLAME, e.getLocation().add(0,1,0), 10, 0.5,0.5,0.5,0.05);
                e.remove();
            }
        }

        w.spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0,1,0), 3, 0.5, 0.5, 0.5, 0);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
        w.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);

        if (hologram != null && hologram.isValid()) hologram.remove();
        if (entity.isValid()) entity.remove();

        manager.onSpawnerDestroyed(this);
    }

    public void forceRemove() {
        alive = false;
        if (spawnTask    != null) spawnTask.cancel();
        if (particleTask != null) particleTask.cancel();
        if (hologram != null && hologram.isValid()) hologram.remove();
        if (entity   != null && entity.isValid())   entity.remove();
    }

    public void minionDied() {
        if (minionCount > 0) minionCount--;
    }

    public boolean isAlive()        { return alive; }
    public Slime getEntity()        { return entity; }
    public UUID getId()             { return id; }
    public Location getLocation()   { return entity != null && entity.isValid() ? entity.getLocation() : null; }
}
