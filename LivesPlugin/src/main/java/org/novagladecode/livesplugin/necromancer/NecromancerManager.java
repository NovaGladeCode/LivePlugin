package org.novagladecode.livesplugin.necromancer;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Manages the Necromancer boss fight in the End.
 * Suppresses the Ender Dragon, spawns Necromancer with a dramatic animation,
 * and handles its multi-phase AI.
 */
public class NecromancerManager implements Listener {

    public static final String BOSS_META    = "necromancer_boss";
    public static final String MINION_META  = "necromancer_minion";
    private static final double MAX_HP = 350.0;

    private static NecromancerManager instance;

    private final JavaPlugin plugin;
    private WitherSkeleton boss;
    private BossBar bossBar;
    private BukkitRunnable aiTask;
    private boolean active = false;
    private boolean enraged = false;

    // Attack cooldowns (ticks elapsed since last use)
    private int cdBoneStorm    = 999;
    private int cdArmySummon   = 999;
    private int cdWitherSkull  = 999;
    private int cdCurse        = 999;
    private int cdVoidPortal   = 999;

    private NecromancerManager(JavaPlugin plugin) { this.plugin = plugin; }

    public static NecromancerManager getInstance(JavaPlugin plugin) {
        if (instance == null) instance = new NecromancerManager(plugin);
        return instance;
    }

    // ── Spawn with animation ─────────────────────────────────────────────────

    public void spawnWithAnimation(Location loc) {
        if (active) {
            Bukkit.broadcastMessage("§5§lThe Necromancer is already present!");
            return;
        }

        Bukkit.broadcastMessage("§5§l════════════════════════════════════");
        Bukkit.broadcastMessage("§5§l  A DARK PRESENCE STIRS IN THE END...");
        Bukkit.broadcastMessage("§5§l════════════════════════════════════");

        // Remove any existing Ender Dragon
        suppressEnderDragons(loc.getWorld());

        // Phase 1: ground rumble animation (10 seconds)
        runSpawnAnimation(loc, () -> finishSpawn(loc));
    }

    private void runSpawnAnimation(Location loc, Runnable onFinish) {
        new BukkitRunnable() {
            int tick = 0;
            final int DURATION = 200; // 10 seconds

            @Override public void run() {
                World w = loc.getWorld();

                // Rising pillar of particles
                double height = (tick / (double) DURATION) * 6.0;
                for (int i = 0; i < 360; i += 20) {
                    double angle = Math.toRadians(i + tick * 5);
                    double radius = 3.0 - (tick / (double) DURATION) * 2.5;
                    Location pl = loc.clone().add(Math.cos(angle) * radius, height * 0.5, Math.sin(angle) * radius);
                    w.spawnParticle(Particle.PORTAL, pl, 2, 0.05, 0.05, 0.05, 0);
                    w.spawnParticle(Particle.DRAGON_BREATH, pl, 1, 0.02, 0.02, 0.02, 0);
                }

                // Dramatic sounds at intervals
                if (tick == 0)   w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.4f);
                if (tick == 60)  w.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.5f);
                if (tick == 120) w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.6f);
                if (tick == 160) w.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 0.5f);

                // Broadcast warnings
                if (tick == 60)  Bukkit.broadcastMessage("§5§l★ The void trembles with unholy power...");
                if (tick == 120) Bukkit.broadcastMessage("§5§l★ Ancient bones rise from the darkness...");
                if (tick == 180) Bukkit.broadcastMessage("§5§l★ THE NECROMANCER AWAKENS!");

                tick += 5;
                if (tick >= DURATION) {
                    cancel();
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void finishSpawn(Location loc) {
        active = true;
        World w = loc.getWorld();

        // Big reveal effect
        w.strikeLightningEffect(loc);
        w.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2f, 0.5f);
        w.spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0, 1, 0), 5, 1, 1, 1, 0);

        // Spawn Wither Skeleton boss
        boss = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        boss.setMetadata(BOSS_META, new FixedMetadataValue(plugin, true));
        boss.setCustomName("§5§l☠ THE NECROMANCER ☠");
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setGlowing(true);

        // Scale up (requires Paper 1.21+)
        var scale = boss.getAttribute(Attribute.GENERIC_SCALE);
        if (scale != null) scale.setBaseValue(2.5);

        var maxHp = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHp != null) maxHp.setBaseValue(MAX_HP);
        boss.setHealth(MAX_HP);

        var speed = boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.3);

        var atk = boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(10.0);

        var armor = boss.getAttribute(Attribute.GENERIC_ARMOR);
        if (armor != null) armor.setBaseValue(8.0);

        // Equipment — End-themed
        boss.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.NETHERITE_HELMET));
        boss.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(Material.NETHERITE_CHESTPLATE));
        boss.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.BONE));

        // Bossbar
        bossBar = Bukkit.createBossBar("§5§l☠ THE NECROMANCER §7| §d" + (int) MAX_HP + " HP",
                BarColor.PURPLE, BarStyle.SEGMENTED_6);
        for (Player p : Bukkit.getOnlinePlayers()) bossBar.addPlayer(p);

        Bukkit.broadcastMessage("§5§l════════════════════════════════════");
        Bukkit.broadcastMessage("§5§l  ☠ THE NECROMANCER HAS APPEARED! ☠");
        Bukkit.broadcastMessage("§5§l════════════════════════════════════");

        startAI();
    }

    // ── AI Loop ──────────────────────────────────────────────────────────────

    private void startAI() {
        aiTask = new BukkitRunnable() {
            @Override public void run() {
                if (boss == null || !boss.isValid() || boss.isDead()) { cancel(); return; }

                double ratio = boss.getHealth() / MAX_HP;
                bossBar.setProgress(Math.max(0, ratio));
                bossBar.setTitle("§5§l☠ THE NECROMANCER §7| §d" + (int) boss.getHealth() + " / " + (int) MAX_HP + " HP");

                // Enrage at 30%
                if (!enraged && ratio <= 0.30) {
                    enterEnrage();
                }

                // Ambient particles
                boss.getWorld().spawnParticle(Particle.DRAGON_BREATH, boss.getLocation().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0.02);

                Player target = nearestPlayer();
                if (target != null) boss.setTarget(target);

                cdBoneStorm++; cdArmySummon++; cdWitherSkull++; cdCurse++; cdVoidPortal++;

                if (target == null) return;

                // Attack rotation
                if (cdWitherSkull >= 60) {
                    launchWitherSkulls(target);
                    cdWitherSkull = 0;
                } else if (cdBoneStorm >= 80) {
                    boneStorm(target);
                    cdBoneStorm = 0;
                } else if (cdArmySummon >= 200) {
                    summonSkeletons();
                    cdArmySummon = 0;
                } else if (cdCurse >= 150) {
                    witherCurse();
                    cdCurse = 0;
                } else if (cdVoidPortal >= 180) {
                    voidPortal(target);
                    cdVoidPortal = 0;
                }
            }
        };
        aiTask.runTaskTimer(plugin, 10L, 20L);
    }

    // ── Attacks ──────────────────────────────────────────────────────────────

    private void launchWitherSkulls(Player target) {
        Location origin = boss.getLocation().add(0, 2, 0);
        World w = origin.getWorld();
        Vector dir = target.getLocation().add(0, 1, 0).subtract(origin).toVector().normalize();
        for (int i = 0; i < 3; i++) {
            final int shot = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!boss.isValid()) return;
                Vector v = dir.clone().add(new Vector((Math.random()-0.5)*0.2, 0, (Math.random()-0.5)*0.2));
                WitherSkull skull = w.spawn(origin, WitherSkull.class);
                skull.setDirection(v.multiply(0.8));
                skull.setCharged(enraged);
            }, shot * 5L);
        }
        w.playSound(origin, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.8f);
    }

    private void boneStorm(Player target) {
        Location loc = boss.getLocation();
        World w = loc.getWorld();
        w.playSound(loc, Sound.ENTITY_SKELETON_SHOOT, 1.5f, 0.6f);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            Vector dir = new Vector(Math.cos(angle), 0.2, Math.sin(angle)).normalize().multiply(1.2);
            Arrow arrow = w.spawn(loc.clone().add(0, 2, 0), Arrow.class);
            arrow.setShooter(boss);
            arrow.setVelocity(dir);
            arrow.setDamage(6.0);
        }
        w.spawnParticle(Particle.BONE_MEAL, loc.clone().add(0, 1, 0), 40, 1, 1, 1, 0.1);
    }

    private void summonSkeletons() {
        Location loc = boss.getLocation();
        World w = loc.getWorld();
        Bukkit.broadcastMessage("§5§l☠ The Necromancer raises his army!");
        for (int i = 0; i < 4; i++) {
            double angle = Math.toRadians(i * 90);
            Location sl = loc.clone().add(Math.cos(angle) * 3, 0, Math.sin(angle) * 3);
            Skeleton sk = (Skeleton) w.spawnEntity(sl, EntityType.SKELETON);
            sk.setMetadata(MINION_META, new FixedMetadataValue(plugin, true));
            sk.setCustomName("§5☠ Undead Archer");
            sk.setCustomNameVisible(true);
            var hp = sk.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (hp != null) hp.setBaseValue(25);
            sk.setHealth(25);
            w.spawnParticle(Particle.SOUL, sl.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        }
        w.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 1f, 0.5f);
    }

    private void witherCurse() {
        Location loc = boss.getLocation();
        World w = loc.getWorld();
        w.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 2f, 0.3f);
        w.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 1, 0), 50, 2, 1, 2, 0.05);
        for (Entity e : boss.getNearbyEntities(15, 15, 15)) {
            if (e instanceof Player p) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.WITHER, 100, 1));
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 80, 1));
                p.sendMessage("§5§l☠ WITHERING CURSE!");
            }
        }
    }

    private void voidPortal(Player target) {
        World w = boss.getWorld();
        // Teleport target to a random spot near the boss
        double angle = Math.random() * Math.PI * 2;
        double radius = 8 + Math.random() * 5;
        Location dest = boss.getLocation().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        dest = w.getHighestBlockAt(dest).getLocation().add(0, 1, 0);
        w.spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        target.teleport(dest);
        w.spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        w.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        target.sendMessage("§5§lVOID PORTAL! You have been displaced!");
    }

    private void enterEnrage() {
        enraged = true;
        bossBar.setColor(BarColor.PURPLE);
        var speed = boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.42);
        var atk = boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(16.0);
        Bukkit.broadcastMessage("§5§l☠ THE NECROMANCER ENTERS DEATH'S EMBRACE! ☠");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 0.4f);
        boss.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, boss.getLocation().add(0,1,0), 80, 2, 2, 2, 0.1);
    }

    // ── Events ────────────────────────────────────────────────────────────────

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().hasMetadata(BOSS_META)) {
            e.getDrops().clear();
            Bukkit.broadcastMessage("§5§l════════════════════════════════════");
            Bukkit.broadcastMessage("§5§l  ☠ THE NECROMANCER HAS BEEN SLAIN! ☠");
            Bukkit.broadcastMessage("§a§l  The End is cleansed of his dark power!");
            Bukkit.broadcastMessage("§5§l════════════════════════════════════");
            cleanup();
        }
        // Remove minions drops
        if (e.getEntity().hasMetadata(MINION_META)) {
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onEnderDragonSpawn(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            // Suppress natural Dragon spawning when Necromancer is active or always
            if (active) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        if (bossBar != null) bossBar.addPlayer(e.getPlayer());
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private void suppressEnderDragons(World w) {
        if (w == null) return;
        for (Entity e : w.getEntities()) {
            if (e instanceof EnderDragon) {
                w.spawnParticle(Particle.DRAGON_BREATH, e.getLocation(), 60, 3, 3, 3, 0.05);
                e.remove();
            }
        }
    }

    public void cleanup() {
        active = false;
        enraged = false;
        if (aiTask != null) { aiTask.cancel(); aiTask = null; }
        if (bossBar != null) { bossBar.removeAll(); bossBar = null; }
        // Remove minions
        if (boss != null && boss.isValid()) {
            for (Entity e : boss.getNearbyEntities(60, 60, 60)) {
                if (e.hasMetadata(MINION_META)) e.remove();
            }
            boss.remove();
            boss = null;
        }
    }

    private Player nearestPlayer() {
        if (boss == null) return null;
        return boss.getNearbyEntities(60, 60, 60).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(boss.getLocation())))
                .orElse(null);
    }

    public boolean isActive() { return active; }
}
