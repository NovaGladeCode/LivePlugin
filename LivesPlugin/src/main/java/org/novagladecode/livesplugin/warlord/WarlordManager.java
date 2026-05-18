package org.novagladecode.livesplugin.warlord;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Central coordinator for the Warlord fight.
 * Manages phase transitions and owns all sub-objects.
 */
public class WarlordManager implements Listener {

    public enum Phase { INACTIVE, PHASE1, PHASE2, PHASE3 }

    private static WarlordManager instance;

    private final JavaPlugin plugin;
    private Phase phase = Phase.INACTIVE;

    private WarlordBoss boss;
    private final List<WarlordSpawner> spawners = new ArrayList<>();

    // Player → assigned spawner (for compass)
    private final Map<UUID, WarlordSpawner> compassAssignments = new HashMap<>();
    private WarlordCompass compassRunnable;

    private WarlordManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static WarlordManager getInstance(JavaPlugin plugin) {
        if (instance == null) instance = new WarlordManager(plugin);
        return instance;
    }

    // ── Start Fight ──────────────────────────────────────────────────────────

    public boolean startFight(Location loc) {
        if (phase != Phase.INACTIVE) return false;
        phase = Phase.PHASE1;
        Bukkit.broadcastMessage("§4§l⚔ THE WARLORD HAS AWAKENED! ⚔");
        Bukkit.broadcastMessage("§cPrepare yourselves! A legendary warrior descends upon you!");
        boss = new WarlordBoss(plugin, this);
        boss.spawn(loc);
        // Begin health check loop that detects phase transition
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkPhaseTransition, 20L, 20L);
        return true;
    }

    private void checkPhaseTransition() {
        if (phase != Phase.PHASE1 || boss == null || !boss.isAlive()) return;
        double ratio = boss.getEntity().getHealth() / WarlordConfig.BOSS_MAX_HEALTH;
        if (ratio <= WarlordConfig.PHASE2_HEALTH_THRESHOLD) {
            transitionToPhase2();
        }
    }

    // ── Phase 2 ──────────────────────────────────────────────────────────────

    private void transitionToPhase2() {
        phase = Phase.PHASE2;
        boss.enterPhase2();

        Bukkit.broadcastMessage("§5§l════════════════════════════════════");
        Bukkit.broadcastMessage("§5§l  THE WARLORD RETREATS INTO SHADOW!");
        Bukkit.broadcastMessage("§e§l  Destroy the SPAWNERS to bring him back!");
        Bukkit.broadcastMessage("§5§l════════════════════════════════════");

        Location bossLoc = boss.getEntity().getLocation();
        int playerCount = Bukkit.getOnlinePlayers().size();
        int count = WarlordConfig.SPAWNER_COUNT_BASE + (playerCount / 2);

        // Spawn spawners in a circle around boss location
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(i * (360.0 / count));
            int radius = WarlordConfig.SPAWNER_RADIUS;
            Location sl = bossLoc.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            sl = findGround(sl);
            WarlordSpawner sp = new WarlordSpawner(plugin, sl, this);
            spawners.add(sp);
        }

        Bukkit.broadcastMessage("§c§l" + count + " Warlord Spawners have appeared!");

        // Assign spawners to players (round-robin) and start compass
        List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (int i = 0; i < players.size(); i++) {
            compassAssignments.put(players.get(i).getUniqueId(), spawners.get(i % spawners.size()));
        }
        compassRunnable = new WarlordCompass(plugin, compassAssignments);
        compassRunnable.runTaskTimer(plugin, 0L, WarlordConfig.COMPASS_UPDATE_TICKS);
    }

    // ── Spawner Destroyed Callback ────────────────────────────────────────────

    public void onSpawnerDestroyed(WarlordSpawner sp) {
        long alive = spawners.stream().filter(WarlordSpawner::isAlive).count();
        Bukkit.broadcastMessage("§a§lSPAWNER DESTROYED! §7Remaining: §c" + alive);

        if (alive == 0) {
            transitionToPhase3();
        }
    }

    // ── Phase 3 ──────────────────────────────────────────────────────────────

    private void transitionToPhase3() {
        phase = Phase.PHASE3;
        if (compassRunnable != null) { compassRunnable.cancel(); compassRunnable = null; }
        // Remove compasses
        for (Player p : Bukkit.getOnlinePlayers()) WarlordCompass.removeCompass(p);
        compassAssignments.clear();

        Bukkit.broadcastMessage("§4§l════════════════════════════════════");
        Bukkit.broadcastMessage("§4§l  ALL SPAWNERS DESTROYED!");
        Bukkit.broadcastMessage("§c§l  THE WARLORD RETURNS - STRONGER!");
        Bukkit.broadcastMessage("§4§l════════════════════════════════════");

        Bukkit.getScheduler().runTaskLater(plugin, boss::enterPhase3, 60L); // 3s delay for drama
    }

    // ── Fight Over ────────────────────────────────────────────────────────────

    public void onBossKilled() {
        Bukkit.broadcastMessage("§6§l════════════════════════════════════");
        Bukkit.broadcastMessage("§6§l  THE WARLORD HAS BEEN DEFEATED!");
        Bukkit.broadcastMessage("§a§l  Victory belongs to the brave!");
        Bukkit.broadcastMessage("§6§l════════════════════════════════════");
        cleanup();
    }

    public void cleanup() {
        phase = Phase.INACTIVE;
        if (compassRunnable != null) { compassRunnable.cancel(); compassRunnable = null; }
        for (Player p : Bukkit.getOnlinePlayers()) WarlordCompass.removeCompass(p);
        compassAssignments.clear();
        for (WarlordSpawner sp : spawners) sp.forceRemove();
        spawners.clear();
        if (boss != null) { boss.cleanup(); boss = null; }
    }

    // ── Events ────────────────────────────────────────────────────────────────

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Entity dead = e.getEntity();

        // Boss killed
        if (dead.hasMetadata(WarlordBoss.META_KEY) && phase != Phase.INACTIVE) {
            e.getDrops().clear();
            onBossKilled();
            return;
        }

        // Minion died — decrement spawner count
        if (dead.hasMetadata(WarlordAttacks.MINION_META) && dead.hasMetadata("spawner_id")) {
            String sid = dead.getMetadata("spawner_id").get(0).asString();
            spawners.stream()
                    .filter(s -> s.getId().toString().equals(sid))
                    .findFirst().ifPresent(WarlordSpawner::minionDied);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Spawner hit by player
        if (!(e.getDamager() instanceof Player)) return;
        Entity target = e.getEntity();
        if (!target.hasMetadata(WarlordSpawner.META_KEY)) return;
        e.setCancelled(true);

        String sid = target.getMetadata(WarlordSpawner.META_KEY).get(0).asString();
        double dmg  = ((Player) e.getDamager()).getAttribute(
                org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE) != null
                ? ((Player) e.getDamager()).getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).getValue()
                : 5.0;

        spawners.stream()
                .filter(s -> s.getId().toString().equals(sid) && s.isAlive())
                .findFirst().ifPresent(s -> s.damage(dmg));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (boss != null) boss.addBossBarPlayer(e.getPlayer());
        if (phase == Phase.PHASE2) {
            WarlordSpawner assigned = spawners.stream().filter(WarlordSpawner::isAlive).findFirst().orElse(null);
            if (assigned != null) compassAssignments.put(e.getPlayer().getUniqueId(), assigned);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Location findGround(Location loc) {
        World w = loc.getWorld();
        int y = w.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
        return new Location(w, loc.getX(), y + 1, loc.getZ());
    }

    public Phase getPhase() { return phase; }
    public WarlordBoss getBoss() { return boss; }
}
