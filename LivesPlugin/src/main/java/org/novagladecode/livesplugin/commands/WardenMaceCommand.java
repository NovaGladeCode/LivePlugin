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

public class WardenMaceCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public WardenMaceCommand(JavaPlugin plugin, org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
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
                || !item.getItemMeta().getDisplayName().equals("§3Warden Mace")) {
            p.sendMessage("§cYou must hold the Warden Mace to use this command!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /wardenmace <1|2>");
            return true;
        }

        long currentTime = System.currentTimeMillis();

        if (args[0].equals("1")) {
            // Ability 1: Sculk Resonance
            if (cooldown1.containsKey(p.getUniqueId())) {
                long cooldown = cooldown1.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cSculk Resonance is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateResonance(p);
            cooldown1.put(p.getUniqueId(), currentTime + 300000); // 5 minutes
            p.sendMessage("§3Sculk Resonance activated!");

        } else if (args[0].equals("2")) {
            // Ability 2: Warden's Grasp
            if (cooldown2.containsKey(p.getUniqueId())) {
                long cooldown = cooldown2.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cWarden's Grasp is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateGrasp(p);
            cooldown2.put(p.getUniqueId(), currentTime + 240000); // 4 minutes
            p.sendMessage("§3Warden's Grasp activated!");
        }

        return true;
    }

    private void activateResonance(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);

        // Visuals: Expanding circle
        for (int i = 0; i < 20; i++) {
            final int r = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int degree = 0; degree < 360; degree += 10) {
                    double radians = Math.toRadians(degree);
                    double x = Math.cos(radians) * r;
                    double z = Math.sin(radians) * r;
                    p.getWorld().spawnParticle(Particle.SCULK_SOUL, p.getLocation().add(x, 0.5, z), 1);
                    p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(x, 0.5, z), 1);
                }
            }, i * 2L);
        }

        // Effects
        for (Entity e : p.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity && e != p) {
                // Trust check
                if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                    continue;

                LivingEntity le = (LivingEntity) e;
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 160, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
            }
        }
    }

    private void activateGrasp(Player p) {
        // "Warden's Grasp" - Raycast and trap
        Location start = p.getEyeLocation();
        Vector direction = start.getDirection();
        Location target = start.clone();

        // Raycast up to 20 blocks
        for (int i = 0; i < 20; i++) {
            target.add(direction);
            if (target.getBlock().getType().isSolid()) {
                break;
            }
            // Particle trail
            target.getWorld().spawnParticle(Particle.SCULK_SOUL, target, 1, 0, 0, 0, 0);
        }

        // Trap Location
        Location trapLoc = target; // Center of trap

        // Visuals: Erupting Sculk Tendrils
        trapLoc.getWorld().playSound(trapLoc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.0f, 0.5f);
        trapLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, trapLoc.clone().add(0, 1, 0), 1);

        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 4;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location tendril = trapLoc.clone().add(x, 0, z);

            // Pillar of particles
            for (double y = 0; y < 3; y += 0.5) {
                tendril.getWorld().spawnParticle(Particle.SCULK_CHARGE, tendril.clone().add(0, y, 0), 1, 0, 0, 0, 0);
            }
        }

        // Logical Effect: Pull and Damage
        for (Entity e : trapLoc.getWorld().getNearbyEntities(trapLoc, 5, 5, 5)) {
            if (e instanceof LivingEntity && e != p) {
                // Trust check
                if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                    continue;

                LivingEntity le = (LivingEntity) e;

                // Pull towards center
                Vector pull = trapLoc.toVector().subtract(le.getLocation().toVector()).normalize().multiply(0.8);
                le.setVelocity(pull);

                // Heavy debuffs (Grasp)
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4)); // 5s Immobilized
                le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1)); // Wither damage

                le.damage(6.0, p); // Direct damage

                if (le instanceof Player) {
                    ((Player) le).sendMessage("§3You are in the Warden's Grasp!");
                }
            }
        }
    }
}
