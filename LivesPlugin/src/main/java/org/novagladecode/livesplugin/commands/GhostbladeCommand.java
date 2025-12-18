package org.novagladecode.livesplugin.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostbladeCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public GhostbladeCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getItemManager().isGhostblade(item)) {
            p.sendMessage("§cYou must be holding the Ghostblade!");
            return true;
        }

        if (!plugin.isAbilityEnabled("ghostblade")) {
            p.sendMessage("§cGhostblade abilities are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /ghostblade <1|2>");
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
            p.sendMessage("§cAbility on cooldown!");
            return;
        }

        // Haunt: Nausea + Blindness to nearby
        for (Entity e : p.getNearbyEntities(8, 8, 8)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                if (dataManager.isTrusted(p.getUniqueId(), target.getUniqueId()))
                    continue;
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                target.sendMessage("§7§oYou feel a ghostly presence...");
                target.playSound(target.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.0f, 0.5f);
            }
        }
        p.sendMessage("§7You have haunted nearby souls.");
        p.playSound(p.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0f, 0.5f);
        cooldown1.put(p.getUniqueId(), now + 30000); // 30s
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need Forge Level 10 to use this! (Current: " + points + "/10)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown2.getOrDefault(p.getUniqueId(), 0L) > now) {
            p.sendMessage("§cAbility on cooldown!");
            return;
        }

        // Spectral Tether: Hold enemies in place
        for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                if (dataManager.isTrusted(p.getUniqueId(), target.getUniqueId()))
                    continue;

                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 6)); // Slowness VII for 3s
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                target.sendMessage("§7§lYou have been tethered by the Ghostblade!");
                target.playSound(target.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.5f);

                // Spawn particles around target
                target.getWorld().spawnParticle(org.bukkit.Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.5,
                        0.5, 0.5, 0.05);
            }
        }

        p.sendMessage("§7Spectral Tether activated.");
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.5f);
        cooldown2.put(p.getUniqueId(), now + 45000); // 45s
    }
}
