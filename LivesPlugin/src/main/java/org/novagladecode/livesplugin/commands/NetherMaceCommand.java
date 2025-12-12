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

public class NetherMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public NetherMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
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
            useAbility1(p);
        } else if (args[0].equals("2")) {
            useAbility2(p);
        } else {
            p.sendMessage("§cUsage: /nethermace <1|2>");
        }

        return true;
    }

    public void useAbility1(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 5) {
            p.sendMessage("§cYou need 5 Might to use this! (Current: " + points + "/5)");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldown1.containsKey(p.getUniqueId())) {
            long cooldown = cooldown1.get(p.getUniqueId());
            if (currentTime < cooldown) {
                p.sendMessage("§cInfernal Wrath is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateInfernalWrath(p);
        cooldown1.put(p.getUniqueId(), currentTime + 240000); // 4 minutes
        p.sendMessage("§6Infernal Wrath activated!");
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need 10 Might to use this! (Current: " + points + "/10)");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldown2.containsKey(p.getUniqueId())) {
            long cooldown = cooldown2.get(p.getUniqueId());
            if (currentTime < cooldown) {
                p.sendMessage("§cFire Tornado is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateFireTornado(p);
        cooldown2.put(p.getUniqueId(), currentTime + 360000); // 6 minutes
        p.sendMessage("§6Fire Tornado activated!");
    }

    private void activateInfernalWrath(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 2.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_AMBIENT, 2.0f, 1.0f);

        Location center = p.getLocation();

        // Meteor shower over 6 seconds
        for (int wave = 0; wave < 12; wave++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Spawn 5 meteors per wave
                for (int i = 0; i < 5; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double distance = Math.random() * 20;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    // Start relative to player so it looks like it comes from above them
                    Location meteorStart = center.clone().add(x, 30, z);

                    // End on the ground (highest block)
                    Location meteorEnd = center.clone().add(x, 0, z);
                    meteorEnd.setY(p.getWorld().getHighestBlockYAt(meteorEnd) + 1);

                    createMeteor(meteorStart, meteorEnd, p);
                }

                // Play sound each wave
                p.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
            }, wave * 10L);
        }
    }

    private void createMeteor(Location start, Location end, Player p) {
        // Create falling meteor effect - MUCH MORE VISIBLE
        for (int i = 0; i < 20; i++) {
            final int step = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double progress = step / 20.0;
                Location current = start.clone().add(
                        (end.getX() - start.getX()) * progress,
                        (end.getY() - start.getY()) * progress,
                        (end.getZ() - start.getZ()) * progress);

                // MASSIVE meteor particles - make it clearly visible
                current.getWorld().spawnParticle(Particle.FLAME, current, 50, 0.5, 0.5, 0.5, 0.15);
                current.getWorld().spawnParticle(Particle.LAVA, current, 20, 0.4, 0.4, 0.4, 0.1);
                current.getWorld().spawnParticle(Particle.SMOKE, current, 30, 0.5, 0.5, 0.5, 0.08);
                current.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, current, 25, 0.5, 0.5, 0.5, 0.1);
                current.getWorld().spawnParticle(Particle.FALLING_LAVA, current, 15, 0.3, 0.3, 0.3, 0);
                current.getWorld().spawnParticle(Particle.EXPLOSION, current, 2);

                // Add a fireball entity for extra visibility
                if (step % 4 == 0) {
                    org.bukkit.entity.Fireball fireball = current.getWorld().spawn(current,
                            org.bukkit.entity.SmallFireball.class);
                    fireball.setVelocity(new Vector(0, -1, 0));
                    fireball.setYield(2.0f); // Higher yield for damage (careful with griefing - set gamerule if needed)
                    fireball.setIsIncendiary(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (fireball.isValid()) {
                            fireball.remove();
                        }
                    }, 5L);
                }

                // Impact on last step
                if (step == 19) {
                    current.getWorld().spawnParticle(Particle.EXPLOSION, end, 10);
                    current.getWorld().spawnParticle(Particle.LAVA, end, 30, 2, 0.5, 2, 0.2);
                    current.getWorld().playSound(end, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);

                    // Launch blocks up (Reduced count)
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            if (Math.random() > 0.2)
                                continue; // 20% chance per block (reduced from 30%)
                            Location blockLoc = end.clone().add(x, -1, z);
                            Material type = blockLoc.getBlock().getType();

                            if (type != Material.AIR && type != Material.BEDROCK && type != Material.BARRIER
                                    && type != Material.LAVA && type != Material.WATER) {
                                org.bukkit.entity.FallingBlock fb = end.getWorld()
                                        .spawnFallingBlock(end.clone().add(x, 0.5, z), type.createBlockData());
                                Vector velocity = new Vector(x * 0.3, 0.6 + Math.random() * 0.4, z * 0.3);
                                fb.setVelocity(velocity);
                                fb.setDropItem(false); // Don't drop items when they break
                            }
                        }
                    }

                    // Damage nearby entities
                    for (Entity e : end.getWorld().getNearbyEntities(end, 3, 3, 3)) {
                        if (e instanceof LivingEntity && e != p) {
                            if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                                continue;

                            LivingEntity le = (LivingEntity) e;
                            le.damage(18.0, p); // Significantly increased damage (9 hearts)
                            le.setFireTicks(120); // 6 seconds of fire

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

        // Prevent flying kick
        p.setAllowFlight(true);
        p.setFlying(true);

        // Fire tornado follows player for 10 seconds (200 ticks)
        final int[] tick = { 0 };

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            tick[0]++;

            if (tick[0] > 80 || !p.isOnline()) { // 4 seconds (80 ticks)
                task.cancel();
                p.sendMessage("§6Fire Tornado dissipated!");
                // Reset flying if they weren't flying before
                if (!p.getGameMode().toString().equals("CREATIVE") && !p.getGameMode().toString().equals("SPECTATOR")) {
                    p.setFlying(false);
                    p.setAllowFlight(false);
                }
                return;
            }

            Location center = p.getLocation();

            // ENHANCED FIRE TORNADO - Funnel Shape with multiple effects
            double height = 15;

            // Color options for dramatic effect
            org.bukkit.Particle.DustOptions redDust = new org.bukkit.Particle.DustOptions(
                    org.bukkit.Color.fromRGB(255, 50, 0), 2.0f);
            org.bukkit.Particle.DustOptions orangeDust = new org.bukkit.Particle.DustOptions(
                    org.bukkit.Color.fromRGB(255, 165, 0), 2.0f);
            org.bukkit.Particle.DustOptions yellowDust = new org.bukkit.Particle.DustOptions(
                    org.bukkit.Color.fromRGB(255, 255, 0), 1.5f);

            for (double y = 0; y < height; y += 0.15) {
                // Funnel shape: Starts thin, gets wider at top
                double radius = 1.5 + (y * 0.5);

                // 8 spirals for maximum density and coverage
                for (int spiral = 0; spiral < 8; spiral++) {
                    double angle = (tick[0] * 120 + y * 40 + spiral * 45) % 360; // Very fast rotation
                    double rad = Math.toRadians(angle);

                    double x = Math.cos(rad) * radius;
                    double z = Math.sin(rad) * radius;

                    Location particleLoc = center.clone().add(x, y, z);

                    // Multiple layered particle effects - INCREASED DENSITY
                    particleLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc, 5, 0.1, 0.1, 0.1, 0.02);
                    particleLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 3, 0.05, 0.05, 0.05,
                            0.01);
                    particleLoc.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    particleLoc.getWorld().spawnParticle(Particle.LAVA, particleLoc, 1, 0.1, 0.1, 0.1, 0);

                    // Color variations based on height
                    if (y < 5) {
                        particleLoc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0, 0, 0, redDust);
                        particleLoc.getWorld().spawnParticle(Particle.LAVA, particleLoc, 2, 0.1, 0.1, 0.1, 0); // More
                                                                                                               // lava
                                                                                                               // at
                                                                                                               // base
                    } else if (y < 10) {
                        particleLoc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0, 0, 0, orangeDust);
                        particleLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 1, 0.05, 0.05,
                                0.05, 0.01);
                    } else {
                        particleLoc.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, yellowDust);
                        particleLoc.getWorld().spawnParticle(Particle.LARGE_SMOKE, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }

                    // Dripping lava effect at bottom
                    if (y < 3) {
                        particleLoc.getWorld().spawnParticle(Particle.DRIPPING_LAVA, particleLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Inner core effect - additional spiral for density
                if (y % 1 < 0.5) {
                    double coreRadius = radius * 0.3;
                    for (int i = 0; i < 360; i += 30) {
                        double rad = Math.toRadians(i + tick[0] * 10);
                        Location coreLoc = center.clone().add(Math.cos(rad) * coreRadius, y,
                                Math.sin(rad) * coreRadius);
                        coreLoc.getWorld().spawnParticle(Particle.FLAME, coreLoc, 1, 0, 0, 0, 0);
                    }
                }
            }

            // Fire Lightning logic
            if (tick[0] % 15 == 0) { // Every 15 ticks
                double angle = Math.random() * Math.PI * 2;
                double dist = Math.random() * 8;
                double lx = Math.cos(angle) * dist;
                double lz = Math.sin(angle) * dist;
                Location strikeLoc = center.clone().add(lx, 0, lz);

                // Cosmetic lightning effect (vertical beam)
                for (int h = 0; h < 15; h++) {
                    Location beam = strikeLoc.clone().add(0, h, 0);
                    strikeLoc.getWorld().spawnParticle(Particle.DUST, beam, 1, 0.1, 0.1, 0.1, orangeDust);
                    strikeLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, beam, 1, 0.1, 0.1, 0.1, 0);
                }
                strikeLoc.getWorld().playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
            }

            // Launch fire charges every 10 ticks
            if (tick[0] % 10 == 0) {
                for (int i = 0; i < 3; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double fireX = Math.cos(angle) * 5;
                    double fireZ = Math.sin(angle) * 5;

                    Location fireLoc = center.clone().add(fireX, 2, fireZ);
                    org.bukkit.entity.Fireball fireball = center.getWorld().spawn(fireLoc,
                            org.bukkit.entity.SmallFireball.class);
                    Vector direction = center.toVector().subtract(fireLoc.toVector()).normalize();
                    fireball.setDirection(direction);
                    fireball.setShooter(p);
                }
            }

            // Keep entities at 3 blocks distance with gentle push/pull
            for (Entity e : center.getWorld().getNearbyEntities(center, 8, 10, 8)) {
                if (e instanceof LivingEntity && e != p) {
                    if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                        continue;

                    LivingEntity le = (LivingEntity) e;

                    double distance = e.getLocation().distance(center);
                    if (distance <= 8) {
                        le.damage(1.0, p);
                        le.setFireTicks(40);

                        // Maintain 3 block distance gently
                        if (distance < 3) {
                            // Push away gently if too close
                            Vector push = e.getLocation().toVector().subtract(center.toVector()).normalize();
                            push.setY(0.1);
                            e.setVelocity(push.multiply(0.1)); // Reduced push
                        } else if (distance > 3 && distance < 8) {
                            // Pull gently if too far
                            Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize();
                            pull.setY(0.05);
                            e.setVelocity(pull.multiply(0.05)); // Reduced pull
                        }
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
