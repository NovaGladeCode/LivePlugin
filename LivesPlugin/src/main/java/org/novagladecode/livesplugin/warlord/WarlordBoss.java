package org.novagladecode.livesplugin.warlord;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class WarlordBoss {

    public static final String META_KEY = "warlord_boss";

    private final JavaPlugin plugin;
    private final WarlordManager manager;
    private Warden entity;
    private BossBar bossBar;

    // Attack cooldown counters (ticks elapsed)
    private int ticksSinceGroundSlam = 999;
    private int ticksSinceDash       = 999;
    private int ticksSinceSummon     = 999;
    private int ticksSinceShockwave  = 999;
    private int ticksSinceFireball   = 999;
    private int ticksSincePull       = 999;

    private boolean raging = false;
    private int rageTicks  = 0;
    private BukkitRunnable aiTask;

    public WarlordBoss(JavaPlugin plugin, WarlordManager manager) {
        this.plugin  = plugin;
        this.manager = manager;
    }

    public void spawn(Location loc) {
        entity = (Warden) loc.getWorld().spawnEntity(loc, EntityType.WARDEN);
        entity.setMetadata(META_KEY, new FixedMetadataValue(plugin, true));
        entity.setCustomName("§4§l⚔ THE WARLORD ⚔");
        entity.setCustomNameVisible(true);
        entity.setRemoveWhenFarAway(false);

        applyAttributes(false);

        bossBar = Bukkit.createBossBar("§4§l⚔ THE WARLORD §7| §cPhase 1",
                BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.setVisible(true);
        for (Player p : Bukkit.getOnlinePlayers()) bossBar.addPlayer(p);

        startAI();
    }

    private void applyAttributes(boolean phase3) {
        setAttr(Attribute.GENERIC_MAX_HEALTH,      WarlordConfig.BOSS_MAX_HEALTH);
        setAttr(Attribute.GENERIC_MOVEMENT_SPEED,  phase3 ? WarlordConfig.BOSS_SPEED_P3 : WarlordConfig.BOSS_SPEED_P1);
        setAttr(Attribute.GENERIC_ATTACK_DAMAGE,   phase3 ? WarlordConfig.BOSS_ATTACK_P3 : WarlordConfig.BOSS_ATTACK_P1);
        setAttr(Attribute.GENERIC_ARMOR,           WarlordConfig.BOSS_ARMOR);
        if (!phase3) {
            entity.setHealth(WarlordConfig.BOSS_MAX_HEALTH);
        }
    }

    private void setAttr(Attribute attr, double val) {
        var inst = entity.getAttribute(attr);
        if (inst != null) inst.setBaseValue(val);
    }

    private void startAI() {
        aiTask = new BukkitRunnable() {
            @Override public void run() {
                if (entity == null || !entity.isValid() || entity.isDead()) {
                    cancel(); return;
                }

                // Update bossbar
                double ratio = entity.getHealth() / WarlordConfig.BOSS_MAX_HEALTH;
                bossBar.setProgress(Math.max(0, ratio));
                bossBar.setTitle("§4§l⚔ THE WARLORD §7| §c"
                        + (int) entity.getHealth() + " / " + (int) WarlordConfig.BOSS_MAX_HEALTH + " HP");

                // Target nearest player
                Player target = nearestPlayer();
                if (target != null) {
                    entity.increaseAngerAt(target, 150);
                    entity.setTarget(target);
                }

                // Rage check
                if (!raging && ratio <= WarlordConfig.RAGE_HEALTH_THRESHOLD) {
                    startRage();
                }
                if (raging) {
                    rageTicks--;
                    if (rageTicks <= 0) endRage();
                }

                // Increment cooldowns
                ticksSinceGroundSlam++;
                ticksSinceDash++;
                ticksSinceSummon++;
                ticksSinceShockwave++;
                ticksSinceFireball++;
                ticksSincePull++;

                if (target == null) return;

                // Execute attacks
                if (ticksSinceGroundSlam >= WarlordConfig.CD_GROUND_SLAM) {
                    WarlordAttacks.groundSlam(entity, getNearbyPlayers(10));
                    ticksSinceGroundSlam = 0;
                } else if (ticksSinceDash >= WarlordConfig.CD_DASH) {
                    WarlordAttacks.dash(plugin, entity, target);
                    ticksSinceDash = 0;
                } else if (ticksSinceSummon >= WarlordConfig.CD_SUMMON) {
                    WarlordAttacks.summonMinions(plugin, entity);
                    ticksSinceSummon = 0;
                } else if (ticksSinceShockwave >= WarlordConfig.CD_SHOCKWAVE) {
                    WarlordAttacks.shockwave(plugin, entity, getNearbyPlayers(15));
                    ticksSinceShockwave = 0;
                } else if (ticksSinceFireball >= WarlordConfig.CD_FIREBALL) {
                    WarlordAttacks.fireballBarrage(plugin, entity, target);
                    ticksSinceFireball = 0;
                } else if (ticksSincePull >= WarlordConfig.CD_PULL) {
                    WarlordAttacks.pullPlayers(entity, getNearbyPlayers(20));
                    ticksSincePull = 0;
                }
            }
        };
        aiTask.runTaskTimer(plugin, 10L, 20L); // every second
    }

    private void startRage() {
        raging = true;
        rageTicks = WarlordConfig.RAGE_DURATION_TICKS;
        bossBar.setColor(BarColor.RED);
        Bukkit.broadcastMessage("§4§l⚡ THE WARLORD ENTERS RAGE MODE! ⚡");
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WARDEN_ROAR, 2f, 0.6f);
        entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation().add(0, 1, 0), 60, 1, 1, 1, 0.1);
        setAttr(Attribute.GENERIC_MOVEMENT_SPEED, WarlordConfig.BOSS_SPEED_P3);
    }

    private void endRage() {
        raging = false;
        setAttr(Attribute.GENERIC_MOVEMENT_SPEED, WarlordConfig.BOSS_SPEED_P1);
    }

    /** Call when entering phase 2: make invisible and invulnerable. */
    public void enterPhase2() {
        if (aiTask != null) aiTask.cancel();
        entity.setInvulnerable(true);
        entity.setInvisible(true);
        entity.setTarget(null);
        bossBar.setTitle("§8§l⚔ THE WARLORD §7| §8RETREATING...");
        bossBar.setColor(BarColor.PURPLE);
    }

    /** Call when all spawners are destroyed: return stronger. */
    public void enterPhase3() {
        entity.setInvulnerable(false);
        entity.setInvisible(false);
        entity.setHealth(WarlordConfig.BOSS_MAX_HEALTH * 0.6); // return at 60%
        applyAttributes(true);
        bossBar.setTitle("§c§l⚔ THE WARLORD §7| §4Phase 3 - ENRAGED");
        bossBar.setColor(BarColor.RED);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WARDEN_ROAR, 2f, 0.5f);
        entity.getWorld().spawnParticle(Particle.LAVA, entity.getLocation().add(0, 1, 0), 80, 2, 2, 2, 0.1);
        Bukkit.broadcastMessage("§c§l⚔ THE WARLORD HAS RETURNED - STRONGER THAN EVER! ⚔");
        ticksSinceGroundSlam = 999;
        ticksSinceDash = 999;
        ticksSinceSummon = 999;
        ticksSinceShockwave = 999;
        ticksSinceFireball = 999;
        ticksSincePull = 999;
        startAI();
    }

    public void cleanup() {
        if (aiTask != null) { aiTask.cancel(); aiTask = null; }
        if (bossBar != null) { bossBar.removeAll(); bossBar = null; }
        if (entity != null && entity.isValid()) entity.remove();
    }

    public void addBossBarPlayer(Player p) {
        if (bossBar != null) bossBar.addPlayer(p);
    }

    public Warden getEntity() { return entity; }
    public boolean isAlive() { return entity != null && entity.isValid() && !entity.isDead(); }

    private Player nearestPlayer() {
        return entity.getNearbyEntities(60, 60, 60).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(entity.getLocation())))
                .orElse(null);
    }

    private List<Player> getNearbyPlayers(double radius) {
        List<Player> list = new ArrayList<>();
        for (Entity e : entity.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player) list.add((Player) e);
        }
        return list;
    }
}
