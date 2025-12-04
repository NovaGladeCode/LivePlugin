package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.UUID;

public class LifeCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;

    public LifeCommand(PlayerDataManager dataManager) {
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

        if (args.length == 0 || !args[0].equalsIgnoreCase("withdraw")) {
            p.sendMessage("§cUsage: /life withdraw");
            return true;
        }

        int level = dataManager.getLevel(uuid);
        if (level < 1) {
            p.sendMessage("§cYou need at least 1 level to withdraw a life!");
            return true;
        }

        // Convert 1 level to 1 life
        dataManager.setLevel(uuid, level - 1);
        int lives = dataManager.getLives(uuid);
        dataManager.setLives(uuid, lives + 1);
        dataManager.saveData();

        p.sendMessage("§aYou converted 1 level into 1 life!");
        p.sendMessage("§6Level: " + (level - 1) + " | Lives: " + (lives + 1));

        return true;
    }
}
