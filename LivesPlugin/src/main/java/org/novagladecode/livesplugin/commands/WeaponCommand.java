package org.novagladecode.livesplugin.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.novagladecode.livesplugin.logic.ItemManager;

public class WeaponCommand implements CommandExecutor {

    private final ItemManager itemManager;

    public WeaponCommand(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player p = (Player) sender;

        if (!p.isOp()) {
            p.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            p.sendMessage("§cUsage: /weapon give <warden|nether|chickenbow>");
            return true;
        }

        ItemStack weapon = null;
        String type = args[1].toLowerCase();

        switch (type) {
            case "warden":
                weapon = itemManager.createWardenMace();
                break;
            case "nether":
                weapon = itemManager.createNetherMace();
                break;
            case "chickenbow":
                weapon = itemManager.createChickenBow();
                break;
            default:
                p.sendMessage("§cUnknown weapon. Usage: /weapon give <warden|nether|chickenbow>");
                return true;
        }

        if (weapon != null) {
            p.getInventory().addItem(weapon);
            p.sendMessage("§aGiven " + type + " to your inventory.");
        }

        return true;
    }
}
