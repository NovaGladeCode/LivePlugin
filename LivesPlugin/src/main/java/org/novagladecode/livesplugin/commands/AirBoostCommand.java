package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.UUID;

public class AirBoostCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public AirBoostCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        int level = dataManager.getLevel(uuid);
        if (level < 15) {
            p.sendMessage("§cYou need to be level 15 or higher to use this command!");
            return true;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long cooldown = cooldowns.get(uuid);
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 1000;
                p.sendMessage("§cAirBoost is on cooldown! Try again in " + remaining + " seconds.");
                return true;
            }
        }

        // Apply boost
        p.setVelocity(p.getVelocity().add(new Vector(0, 2.0, 0)));
        p.sendMessage("§bWhoosh! You've been boosted!");

        // Apply slowness 5 for 10 seconds
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOWNESS,
                200, // 10 seconds
                4, // Level 5 (amplifier 4)
                false,
                true));

        // Set cooldown (15 seconds = 15000 ms)
        cooldowns.put(uuid, currentTime + 15000);

        return true;
    }
}
