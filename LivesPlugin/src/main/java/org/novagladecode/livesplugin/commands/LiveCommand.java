package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.ItemManager;

import java.util.UUID;

public class LiveCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final ItemManager itemManager;

    public LiveCommand(PlayerDataManager dataManager, ItemManager itemManager) {
        this.dataManager = dataManager;
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage("§cUsage: /life <reset|withdraw>");
            return true;
        }

        UUID uuid = p.getUniqueId();

        if (args[0].equalsIgnoreCase("reset")) {
            // Check if player is OP
            if (!p.isOp()) {
                p.sendMessage("§cYou must be an operator to use this command!");
                return true;
            }

            dataManager.setLevel(uuid, 5);
            dataManager.saveData();

            p.sendMessage("§aYour level has been reset to 5!");
            return true;

        } else if (args[0].equalsIgnoreCase("withdraw")) {
            int level = dataManager.getLevel(uuid);

            if (level < 1) {
                p.sendMessage("§cYou need at least 1 level to withdraw!");
                return true;
            }

            // Deduct 1 level
            dataManager.setLevel(uuid, level - 1);
            dataManager.saveData();

            // Give player a Level Item
            p.getInventory().addItem(itemManager.createLevelItem());
            p.sendMessage("§aYou withdrew 1 level! You now have " + (level - 1) + " levels.");

            return true;
        } else {
            p.sendMessage("§cUsage: /life <reset|withdraw>");
            return true;
        }
    }
}
