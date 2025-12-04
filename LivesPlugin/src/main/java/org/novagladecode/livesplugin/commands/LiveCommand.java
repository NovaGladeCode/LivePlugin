package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.UUID;

public class LiveCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;

    public LiveCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0 || !args[0].equalsIgnoreCase("reset")) {
            p.sendMessage("§cUsage: /live reset");
            return true;
        }

        // Check if player is OP
        if (!p.isOp()) {
            p.sendMessage("§cYou must be an operator to use this command!");
            return true;
        }

        UUID uuid = p.getUniqueId();
        dataManager.setLevel(uuid, 5);
        dataManager.saveData();

        p.sendMessage("§aYour level has been reset to 5!");

        return true;
    }
}
