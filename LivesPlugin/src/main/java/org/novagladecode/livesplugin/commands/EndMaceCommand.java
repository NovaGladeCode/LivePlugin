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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class EndMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public EndMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
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
            // Ability 1: Void Step
            if (cooldown1.containsKey(p.getUniqueId())) {
                long cooldown = cooldown1.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cVoid Step is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateVoidStep(p);
            cooldown1.put(p.getUniqueId(), currentTime + 20000); // 20 seconds
            p.sendMessage("§5Void Step activated!");

        } else if (args[0].equals("2")) {
            // Ability 2: Singularity
            if (cooldown2.containsKey(p.getUniqueId())) {
                long cooldown = cooldown2.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cSingularity is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateSingularity(p);
            cooldown2.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
            p.sendMessage("§5Singularity activated!");
        }

        return true;
    }

    private void activateVoidStep(Player p) {
        Location start = p.getLocation();
        Vector dir = start.getDirection().normalize().multiply(15);
        Location dest = start.clone().add(dir);

        // Find safe landing
        if (dest.getBlock().getType().isSolid()) {
            dest.add(0, 1, 0);
        }

        // Effects at start
        p.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.PORTAL, start, 50, 0.5, 1, 0.5, 0.1);

        // Teleport
        p.teleport(dest);

        // Effects at dest
        p.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.DRAGON_BREATH, dest, 50, 0.5, 1, 0.5, 0.1);

        // Damage enemies in between (Line check)
        Location check = start.clone();
        Vector step = dir.clone().normalize().multiply(1);
        for (int i = 0; i < 15; i++) {
            check.add(step);
            check.getWorld().spawnParticle(Particle.PORTAL, check, 5, 0.2, 0.2, 0.2, 0);
            for (Entity e : check.getWorld().getNearbyEntities(check, 2, 2, 2)) {
                if (e instanceof LivingEntity && e != p) {
                    if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                        continue;

                    LivingEntity le = (LivingEntity) e;
                    le.damage(10, p); // 5 hearts
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                }
            }
        }
    }

    private void activateSingularity(Player p) {
        Location center = p.getLocation().add(0, 5, 0); // Above player
        p.setFlying(true);
        p.setAllowFlight(true);

        p.getWorld().playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 2.0f, 0.5f);
        p.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);

        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!p.isOnline() || ticks >= 200) { // 10 seconds
                    this.cancel();
                    p.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                    p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 5);
                    // Huge explosion finish
                    center.getWorld().createExplosion(center, 0F, false); // Visual only

                    // Reset flight if survival
                    if (!p.getGameMode().toString().equals("CREATIVE")
                            && !p.getGameMode().toString().equals("SPECTATOR")) {
                        p.setFlying(false);
                        p.setAllowFlight(false);
                    }
                    return;
                }

                ticks += 5;

                // Black Hole Visuals
                center.getWorld().spawnParticle(Particle.ASH, center, 100, 1, 1, 1, 0.1);
                center.getWorld().spawnParticle(Particle.PORTAL, center, 50, 2, 2, 2, 0.5);
                center.getWorld().spawnParticle(Particle.SQUID_INK, center, 20, 0.5, 0.5, 0.5, 0.1);

                // Pull Logic
                for (Entity e : center.getWorld().getNearbyEntities(center, 25, 25, 25)) {
                    if (e instanceof LivingEntity && e != p) {
                        if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                            continue;

                        LivingEntity le = (LivingEntity) e;

                        // Violent Pull
                        Vector pull = center.toVector().subtract(le.getLocation().toVector()).normalize().multiply(1.5);
                        le.setVelocity(pull);

                        double dist = le.getLocation().distance(center);
                        if (dist < 5) {
                            // Crushing damage at center
                            le.damage(4.0, p);
                            le.getWorld().spawnParticle(Particle.CRIT, le.getLocation(), 10);
                            le.getWorld().playSound(le.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f,
                                    0.5f); // Crunch sound
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
