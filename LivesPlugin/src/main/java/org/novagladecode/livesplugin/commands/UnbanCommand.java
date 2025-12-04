package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.ItemManager;

import java.util.UUID;

public class UnbanCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final ItemManager itemManager;

    public UnbanCommand(PlayerDataManager dataManager, ItemManager itemManager) {
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
            p.sendMessage("§cUsage: /unban <player>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = dataManager.getPlayerUUIDByName(targetName);

        if (targetUUID == null) {
            p.sendMessage("§cPlayer not found!");
            return true;
        }

        if (!dataManager.isBanned(targetUUID)) {
            p.sendMessage("§cThat player is not banned!");
            return true;
        }

        // Check for unban item in inventory
        boolean hasUnbanItem = false;
        for (ItemStack item : p.getInventory().getContents()) {
            if (itemManager.isUnbanItem(item)) {
                item.setAmount(item.getAmount() - 1);
                hasUnbanItem = true;
                break;
            }
        }

        if (!hasUnbanItem) {
            p.sendMessage("§cYou need an Unban Item to unban players!");
            return true;
        }

        dataManager.setBanned(targetUUID, false);
        dataManager.setLives(targetUUID, 5);
        dataManager.saveData();

        p.sendMessage("§aYou have unbanned " + targetName + "!");
        Bukkit.broadcastMessage("§6" + targetName + " has been unbanned by " + p.getName() + "!");

        return true;
    }
}
