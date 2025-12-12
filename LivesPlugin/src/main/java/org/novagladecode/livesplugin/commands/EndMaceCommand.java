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
                p.sendMessage("§cSingularity is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                return;
            }
        }

        activateSingularity(p);
        cooldown2.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
        p.sendMessage("§5Singularity activated!");
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

    private void activateSingularity(Player p) {
        Location center = p.getLocation().add(0, 5, 0); // Above player
        p.getWorld().playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);

        // Single tick effect? Or duration?
        // User said "inted pull them in ... launch up and away".
        // Sounds like a one-time burst or short duration.
        // I'll make it a 3-second duration ability that pulls, damages, then launches
        // at the end.

        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!p.isOnline() || ticks >= 60) { // 3 seconds
                    // Launch at end
                    for (Entity e : center.getWorld().getNearbyEntities(center, 15, 15, 15)) {
                        if (e instanceof LivingEntity && e != p) {
                            if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                                continue;

                            // Launch Up and Away
                            Vector dir = e.getLocation().toVector().subtract(center.toVector()).normalize();
                            dir.setY(1.0); // Up
                            dir.multiply(1.5); // 6 blocks approx strength? (1.5 velocity is high)
                            e.setVelocity(dir);
                        }
                    }
                    this.cancel();
                    return;
                }

                // Visuals
                center.getWorld().spawnParticle(Particle.SQUID_INK, center, 5, 0.5, 0.5, 0.5, 0);
                center.getWorld().spawnParticle(Particle.PORTAL, center, 10, 1, 1, 1, 0);

                // Pull and Damage
                for (Entity e : center.getWorld().getNearbyEntities(center, 15, 15, 15)) {
                    if (e instanceof LivingEntity && e != p) {
                        if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                            continue;

                        LivingEntity le = (LivingEntity) e;

                        // Mild Pull
                        Vector pull = center.toVector().subtract(le.getLocation().toVector()).normalize().multiply(0.4);
                        le.setVelocity(pull);

                        // Damage every second (20 ticks)
                        if (ticks % 20 == 0) {
                            le.damage(4.0, p); // 2 hearts (User said "do some damage")
                        }
                    }
                }
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
