package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MistbladeCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public MistbladeCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getItemManager().isMistblade(item)) {
            p.sendMessage("§cYou must be holding the Mistblade!");
            return true;
        }

        if (!plugin.isAbilityEnabled("mistblade")) {
            p.sendMessage("§cMistblade abilities are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /mistblade <1|2>");
            return true;
        }

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
            p.sendMessage("§cYou need Forge Level 5 to use this! (Current: " + points + "/5)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown1.getOrDefault(p.getUniqueId(), 0L) > now) {
            long remaining = (cooldown1.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage("§cTrident Storm is on cooldown! (" + remaining + "s)");
            return;
        }

        // Trident Storm: Spawn spinning tridents
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            Vector offset = new Vector(Math.cos(angle) * 3, 3, Math.sin(angle) * 3);
            Trident t = p.getWorld().spawn(p.getLocation().add(offset), Trident.class);
            t.setShooter(p);
            t.setVelocity(new Vector(0, -1, 0));
            t.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED);
            // Particles for each trident
            p.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, t.getLocation(), 20, 0.2, 0.2, 0.2, 0.05);
        }

        p.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, p.getLocation().add(0, 1, 0), 100, 3, 1, 3, 0.1);
        p.getWorld().spawnParticle(org.bukkit.Particle.BUBBLE, p.getLocation().add(0, 1, 0), 50, 2, 1, 2, 0.05);

        p.sendMessage("§bTrident Storm!");
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 0.5f);
        cooldown1.put(p.getUniqueId(), now + 25000); // 25s
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need Forge Level 10 to use this! (Current: " + points + "/10)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown2.getOrDefault(p.getUniqueId(), 0L) > now) {
            long remaining = (cooldown2.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage("§cTidal Surge is on cooldown! (" + remaining + "s)");
            return;
        }

        // Tidal Surge: Massive water blast and lightning
        for (Entity e : p.getNearbyEntities(8, 8, 8)) {
            if (e instanceof org.bukkit.entity.LivingEntity && e != p) {
                org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) e;
                if (target instanceof Player && dataManager.isTrusted(p.getUniqueId(), target.getUniqueId()))
                    continue;

                target.damage(9.0, p);
                target.setVelocity(target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize()
                        .multiply(1.5).setY(0.5));
                target.sendMessage("§9§lYou have been swept away by the Tidal Surge!");

                // Strike with lightning (visual only to avoid extra fire if possible, or real
                // if wanted)
                target.getWorld().strikeLightningEffect(target.getLocation());

                // Target particles
                target.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, target.getLocation().add(0, 1, 0), 60, 0.5,
                        0.5, 0.5, 0.1);
                target.getWorld().spawnParticle(org.bukkit.Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.5,
                        0.5, 0.5, 0.05);
            }
        }

        p.sendMessage("§b§lTidal Surge!");
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.5f, 0.8f);
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);

        p.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, p.getLocation().add(0, 1, 0), 200, 5, 1, 5, 0.2);
        p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation().add(0, 1, 0), 100, 4, 1, 4, 0.1);

        cooldown2.put(p.getUniqueId(), now + 30000); // 30s
    }
}
