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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.scheduler.BukkitRunnable;

public class EndMaceCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    // Invis tracking
    private final Map<UUID, BukkitTask> invisTasks = new HashMap<>();

    public EndMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
                || !item.getItemMeta().getDisplayName().equals("§5End Mace")) {
            p.sendMessage("§cYou must hold the End Mace to use this command!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /endmace <1|2>");
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
            p.sendMessage("§cYou need 5 Might to use this! (Current: " + points + "/5)");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldown1.containsKey(p.getUniqueId())) {
            long cooldown = cooldown1.get(p.getUniqueId());
            if (currentTime < cooldown) {
                p.sendMessage("§cVoid Cloak is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateVoidCloak(p);
        cooldown1.put(p.getUniqueId(), currentTime + 60000); // 1 minute
        p.sendMessage("§5Void Cloak activated!");
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
                p.sendMessage("§cPhantom Assault is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activatePhantomAssault(p);
        cooldown2.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
        p.sendMessage("§5Phantom Assault activated!");
    }

    private void activateVoidCloak(Player p) {
        // Vanish player from everyone
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hidePlayer(plugin, p);
        }

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.ASH, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);

        // Schedule un-vanish
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeInvis(p);
            p.sendMessage("§5Void Cloak expired.");
        }, 200L); // 10 seconds

        invisTasks.put(p.getUniqueId(), task);
    }

    private void removeInvis(Player p) {
        if (invisTasks.containsKey(p.getUniqueId())) {
            invisTasks.remove(p.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, p);
            }
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            p.getWorld().spawnParticle(Particle.ASH, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (invisTasks.containsKey(p.getUniqueId())) {
                // Cancel task and un-invis
                BukkitTask task = invisTasks.get(p.getUniqueId());
                if (!task.isCancelled()) {
                    task.cancel();
                }
                removeInvis(p);
                p.sendMessage("§cAvailable revealed!");
                p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        Player newPlayer = e.getPlayer();
        for (UUID uuid : invisTasks.keySet()) {
            Player invisiblePlayer = Bukkit.getPlayer(uuid);
            if (invisiblePlayer != null) {
                newPlayer.hidePlayer(plugin, invisiblePlayer);
            }
        }
    }

    private void activatePhantomAssault(Player p) {
        Location startLoc = p.getLocation();
        List<LivingEntity> targets = new ArrayList<>();
        // Radius check
        for (Entity e : p.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity && e != p) {
                if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                    continue;
                targets.add((LivingEntity) e);
            }
        }

        if (targets.isEmpty()) {
            p.sendMessage("§cNo targets nearby for Phantom Assault!");
            return;
        }

        p.sendMessage("§5§lVOID PULL INITIATED!");
        p.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.3f);
        p.getWorld().playSound(startLoc, Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.5f);
        
        // Initial burst effect at player location
        for (int i = 0; i < 50; i++) {
            double angle = (2 * Math.PI * i) / 50;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            Location particleLoc = startLoc.clone().add(x, 1, z);
            p.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
            p.getWorld().spawnParticle(Particle.ASH, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
        }

        new BukkitRunnable() {
            int ticks = 0;
            Map<LivingEntity, Boolean> damaged = new HashMap<>();

            @Override
            public void run() {
                if (!p.isOnline() || ticks > 60) { // 3 seconds max
                    // Final explosion effect
                    p.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
                    p.getWorld().spawnParticle(Particle.EXPLOSION, startLoc.clone().add(0, 1, 0), 10, 1, 1, 1, 0.1);
                    p.getWorld().spawnParticle(Particle.END_ROD, startLoc, 100, 2, 2, 2, 0.1);
                    p.getWorld().spawnParticle(Particle.ASH, startLoc, 150, 2, 2, 2, 0.1);
                    this.cancel();
                    return;
                }

                Location playerLoc = p.getLocation().add(0, 1, 0);
                
                // Pull all targets and create visual effects
                for (LivingEntity target : new ArrayList<>(targets)) {
                    if (!target.isValid() || target.isDead()) {
                        continue;
                    }
                    
                    Location targetLoc = target.getLocation().add(0, 1, 0);
                    double distance = targetLoc.distance(playerLoc);
                    
                    // Pull target toward player
                    if (distance > 1.5) {
                        Vector pullDirection = playerLoc.toVector().subtract(targetLoc.toVector()).normalize();
                        double pullStrength = Math.min(0.5, distance * 0.1); // Stronger pull
                        target.setVelocity(pullDirection.multiply(pullStrength).setY(0.1));
                    } else if (!damaged.getOrDefault(target, false)) {
                        // Target is close enough, apply damage
                        target.damage(20.0, p);
                        damaged.put(target, true);
                        
                        // Impact effects
                        p.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 0.8f);
                        p.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.2f);
                        p.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc, 5, 0.5, 0.5, 0.5, 0.1);
                        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 10, 0.8, 0.8, 0.8, 0.1);
                    }
                    
                    // Continuous visual effects on target
                    // Swirling void particles around target
                    double angle = (ticks * 0.2) % (2 * Math.PI);
                    for (int i = 0; i < 8; i++) {
                        double particleAngle = angle + (i * Math.PI / 4);
                        double radius = 1.5;
                        double x = Math.cos(particleAngle) * radius;
                        double z = Math.sin(particleAngle) * radius;
                        Location swirlLoc = targetLoc.clone().add(x, Math.sin(ticks * 0.1) * 0.5, z);
                        p.getWorld().spawnParticle(Particle.END_ROD, swirlLoc, 1, 0, 0, 0, 0);
                        p.getWorld().spawnParticle(Particle.ASH, swirlLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    }
                    
                    // Pull line effect between player and target
                    Vector direction = playerLoc.toVector().subtract(targetLoc.toVector()).normalize();
                    for (int i = 0; i < (int)distance; i++) {
                        Location lineLoc = targetLoc.clone().add(direction.clone().multiply(i));
                        p.getWorld().spawnParticle(Particle.END_ROD, lineLoc, 1, 0, 0, 0, 0);
                        if (i % 2 == 0) {
                            p.getWorld().spawnParticle(Particle.ASH, lineLoc, 1, 0.05, 0.05, 0.05, 0.01);
                        }
                    }
                    
                    // Target location effects
                    p.getWorld().spawnParticle(Particle.CRIT, targetLoc, 5, 0.3, 0.3, 0.3, 0.05);
                    p.getWorld().spawnParticle(Particle.END_ROD, targetLoc, 3, 0.2, 0.2, 0.2, 0.02);
                }
                
                // Player location effects
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI * i) / 20 + (ticks * 0.1);
                    double radius = 1.5 + Math.sin(ticks * 0.2) * 0.3;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location orbitLoc = playerLoc.clone().add(x, 0, z);
                    p.getWorld().spawnParticle(Particle.END_ROD, orbitLoc, 1, 0, 0, 0, 0);
                    p.getWorld().spawnParticle(Particle.ASH, orbitLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick for smooth pulling
    }
}
