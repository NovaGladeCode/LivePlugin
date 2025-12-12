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
    private final org.novagladecode.livesplugin.data.PlayerDataManager dataManager;

    public WeaponCommand(ItemManager itemManager, org.novagladecode.livesplugin.gui.RecipeGUI recipeGUI,
            org.novagladecode.livesplugin.data.PlayerDataManager dataManager) {
        this.itemManager = itemManager;
        this.recipeGUI = recipeGUI;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Allow console to use point set? Yes.
            // But existing code blocks console at start. I'll modifying check.
        }

        // Handling checking inside subcommands if player is needed.

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /weapon <give|recipe|point> [args]");
            return true;
        }

        if (args[0].equalsIgnoreCase("recipe")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            recipeGUI.openMainMenu((Player) sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("point")) {
            if (!sender.isOp()) {
                sender.sendMessage("§cYou do not have permission.");
                return true;
            }
            // /weapon point set <player> <amount>
            if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                sender.sendMessage("§cUsage: /weapon point set <player> <amount>");
                return true;
            }

            Player target = org.bukkit.Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[3]);
                if (amount < 0 || amount > 10) {
                    sender.sendMessage("§cAmount must be between 0 and 10.");
                    return true;
                }
                dataManager.setPoints(target.getUniqueId(), amount);
                sender.sendMessage("§aSet " + target.getName() + "'s Might to " + amount);
                target.sendMessage("§aYour Might was set to " + amount + " by an admin.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            Player p = (Player) sender;
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

        sender.sendMessage("§cUsage: /weapon <give|recipe|point> [name]");
        return true;
    }
}
