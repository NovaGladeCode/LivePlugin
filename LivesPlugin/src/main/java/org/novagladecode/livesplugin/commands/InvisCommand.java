package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.EffectManager;

import java.util.UUID;

public class InvisCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PlayerDataManager dataManager;
    private final EffectManager effectManager;

    public InvisCommand(JavaPlugin plugin, PlayerDataManager dataManager, EffectManager effectManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.effectManager = effectManager;
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
        if (level < 9) {
            p.sendMessage("§cYou need to be level 9 or higher to use this command!");
            return true;
        }

        long cooldown = dataManager.getInvisCooldown(uuid);
        long currentTime = System.currentTimeMillis();

        if (currentTime < cooldown) {
            long remaining = (cooldown - currentTime) / 1000;
            p.sendMessage("§cInvisibility is on cooldown! Try again in " + remaining + " seconds.");
            return true;
        }

        // Apply invisibility
        effectManager.addInvisibility(p);
        p.sendMessage("§aInvisibility enabled for 30 seconds!");

        // Set cooldown (3 minutes = 180000 ms)
        dataManager.setInvisCooldown(uuid, currentTime + 180000);

        // Schedule removal after 30 seconds (600 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            effectManager.removeInvisibility(p);
            p.sendMessage("§cInvisibility has expired!");
        }, 600L);

        return true;
    }
}
