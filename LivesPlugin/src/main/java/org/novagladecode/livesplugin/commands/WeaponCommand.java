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
    private final org.novagladecode.livesplugin.gui.RecipeGUI recipeGUI;

    public WeaponCommand(ItemManager itemManager, org.novagladecode.livesplugin.gui.RecipeGUI recipeGUI) {
        this.itemManager = itemManager;
        this.recipeGUI = recipeGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage("§cUsage: /weapon <give|recipe> [name]");
            return true;
        }

        if (args[0].equalsIgnoreCase("recipe")) {
            recipeGUI.openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!p.isOp()) {
                p.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                p.sendMessage("§cUsage: /weapon give <warden|nether|end|chickenbow>");
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
                case "end":
                    weapon = itemManager.createEndMace();
                    break;
                default:
                    p.sendMessage("§cUnknown weapon. Usage: /weapon give <warden|nether|end|chickenbow>");
                    return true;
            }

            if (weapon != null) {
                p.getInventory().addItem(weapon);
                p.sendMessage("§aGiven " + type + " to your inventory.");
            }
            return true;
        }

        p.sendMessage("§cUsage: /weapon <give|recipe> [name]");
        return true;
    }
}
