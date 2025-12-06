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

            activateSonicBoom(p);
            cooldown1.put(p.getUniqueId(), currentTime + 10000); // 10 seconds cooldown (shorter for an attack)
            p.sendMessage("§3Sonic Boom unleashed!");

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

    private void activateSonicBoom(Player p) {
        Location start = p.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 1.5f);

        // Raycast
        for (double d = 0; d < 20; d += 0.5) {
            Location loc = start.clone().add(direction.clone().multiply(d));

            // Visuals
            if (d % 1.0 == 0) { // Every block
                p.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);
            }
            p.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 1, 0.1, 0.1, 0.1, 0.0);

            // Collision
            if (loc.getBlock().getType().isSolid()) {
                p.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
                break;
            }

            // Entity Hit
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0)) {
                if (e instanceof LivingEntity && e != p) {
                    // Trust check
                    if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                        continue;

                    LivingEntity target = (LivingEntity) e;
                    target.damage(16.0, p); // 8 Hearts (Magic/Sonic damage)
                    target.setVelocity(direction.clone().multiply(2.5).setY(0.5)); // Heavy knockback

                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 1.0f);
                    // Single hit per entity per beam? Or piercing?
                    // Raycast continues (Piercing)
                }
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
            // Dense particle trail
            if (i % 2 == 0) {
                target.getWorld().spawnParticle(Particle.SCULK_SOUL, target, 1, 0.1, 0.1, 0.1, 0.01);
            }
            if (i % 5 == 0) {
                target.getWorld().playSound(target, Sound.ENTITY_WARDEN_HEARTBEAT, 0.5f, 1.5f);
            }
        }

        // Trap Location
        Location trapLoc = target; // Center of trap
        trapLoc.getWorld().playSound(trapLoc, Sound.ENTITY_WARDEN_EMERGE, 1.5f, 0.5f);
        trapLoc.getWorld().playSound(trapLoc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.5f, 0.6f);

        // Massive Deep Dark Expansion
        // Ground spread (Circle)
        for (int degree = 0; degree < 360; degree += 15) {
            double rad = Math.toRadians(degree);
            double x = Math.cos(rad) * 4.5;
            double z = Math.sin(rad) * 4.5;
            Location floorInfo = trapLoc.clone().add(x, 0, z);
            trapLoc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, floorInfo, 1, 0.1, 0, 0.1, 0.05);
            trapLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, floorInfo.add(0, 0.5, 0), 1, 0.1, 0.1, 0.1, 0.02);
        }

        // Erupting Tendrils/Pillars
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 4.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location tendril = trapLoc.clone().add(x, 0, z);

            for (double y = 0; y < 4; y += 0.4) {
                tendril.getWorld().spawnParticle(Particle.SCULK_CHARGE, tendril.clone().add(0, y, 0), 1, 0.1, 0.1, 0.1,
                        0, 0.0f);
            }
        }

        // Sonic Boom burst at center
        trapLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, trapLoc.clone().add(0, 1.5, 0), 3);

        // "Make like attack" - Summon Evoker Fangs (The Maw)
        for (int i = 0; i < 12; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 3.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location biteLoc = trapLoc.clone().add(x, 0, z);
            // Ensure on ground (simple approximation)
            biteLoc.getWorld().spawn(biteLoc, org.bukkit.entity.EvokerFangs.class);
        }

        // Logical Effect: Pull and Damage
        for (Entity e : trapLoc.getWorld().getNearbyEntities(trapLoc, 6, 6, 6)) {
            if (e instanceof LivingEntity && e != p) {
                // Trust check
                if (e instanceof Player && dataManager.isTrusted(p.getUniqueId(), e.getUniqueId()))
                    continue;

                LivingEntity le = (LivingEntity) e;

                // Pull towards center
                Vector pull = trapLoc.toVector().subtract(le.getLocation().toVector()).normalize().multiply(1.2); // Stronger
                                                                                                                  // pull
                le.setVelocity(pull);

                // Heavy debuffs (Grasp)
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4)); // 5s Immobilized
                le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1)); // Wither damage
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0)); // Extra disorientation

                le.damage(8.0, p); // 4 hearts direct damage

                if (le instanceof Player) {
                    ((Player) le).sendMessage("§3§lTHE ABYSS CONSUMES YOU!");
                }

                // Play individual sound for victim
                le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 1.0f);
            }
        }
    }
}
