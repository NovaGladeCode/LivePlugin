package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.UUID;

public class LevelCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;

    public LevelCommand(PlayerDataManager dataManager) {
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
        int lives = dataManager.getLives(uuid);

        p.sendMessage("§6=== Your Stats ===");
        p.sendMessage("§aLevel: " + level);
        p.sendMessage("§cLives: " + lives);

        return true;
    }
}
