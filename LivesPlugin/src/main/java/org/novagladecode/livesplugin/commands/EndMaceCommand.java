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

        p.sendMessage("§5§lPHANTOM ASSAULT INITIATED!");
        p.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= targets.size() || !p.isOnline()) {
                    // Finish
                    p.teleport(startLoc);
                    p.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    this.cancel();
                    return;
                }

                LivingEntity target = targets.get(index);
                if (target.isValid()) {
                    // Teleport behind
                    Location strikeLoc = target.getLocation().add(target.getLocation().getDirection().multiply(-1)); // Behind
                    strikeLoc.setDirection(target.getLocation().toVector().subtract(strikeLoc.toVector())); // Face
                                                                                                            // target
                    p.teleport(strikeLoc);
                    p.getWorld().playSound(strikeLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
                    
                    // Enhanced particle effects
                    Location targetLoc = target.getLocation().add(0, 1, 0);
                    p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 5, 0.5, 0.5, 0.5, 0.1);
                    p.getWorld().spawnParticle(Particle.ASH, targetLoc, 30, 0.8, 0.8, 0.8, 0.1);
                    p.getWorld().spawnParticle(Particle.END_ROD, targetLoc, 15, 0.5, 0.5, 0.5, 0.05);
                    p.getWorld().spawnParticle(Particle.CRIT, targetLoc, 20, 0.6, 0.6, 0.6, 0.1);
                    
                    target.damage(20.0, p); // 10 hearts (doubled from 5)
                }
                index++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Every 5 ticks
    }
}
