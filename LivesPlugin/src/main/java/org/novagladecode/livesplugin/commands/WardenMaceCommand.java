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
    private final HashMap<UUID, Long> cooldown1 = new HashMap<>();
    private final HashMap<UUID, Long> cooldown2 = new HashMap<>();

    public WardenMaceCommand(JavaPlugin plugin) {
        this.plugin = plugin;
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
            // Ability 2: Sonic Beam
            if (cooldown2.containsKey(p.getUniqueId())) {
                long cooldown = cooldown2.get(p.getUniqueId());
                if (currentTime < cooldown) {
                    p.sendMessage("§cSonic Beam is on cooldown! " + (cooldown - currentTime) / 1000 + "s left.");
                    return true;
                }
            }

            activateBeam(p);
            cooldown2.put(p.getUniqueId(), currentTime + 240000); // 4 minutes
            p.sendMessage("§bSonic Beam fired!");
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
                LivingEntity le = (LivingEntity) e;
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 160, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 160, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
            }
        }
    }

    private void activateBeam(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f);

        Location origin = p.getEyeLocation();
        Vector direction = origin.getDirection();

        // Raytrace visuals
        for (double i = 0; i < 30; i += 0.5) {
            Location point = origin.clone().add(direction.clone().multiply(i));
            p.getWorld().spawnParticle(Particle.SCULK_SOUL, point, 2, 0.1, 0.1, 0.1, 0);
            p.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1);
        }

        // Hit detection
        org.bukkit.util.RayTraceResult result = p.getWorld().rayTraceEntities(origin, direction, 30,
                entity -> entity instanceof LivingEntity && entity != p);

        if (result != null && result.getHitEntity() != null) {
            LivingEntity target = (LivingEntity) result.getHitEntity();
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1));
            target.damage(10, p); // Optional damage
        }
    }
}
