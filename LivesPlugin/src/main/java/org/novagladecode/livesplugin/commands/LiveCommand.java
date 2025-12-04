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
            sendHelp(p);
            return true;
        }

        UUID uuid = p.getUniqueId();

        if (args[0].equalsIgnoreCase("help")) {
            sendHelp(p);
            return true;

        } else if (args[0].equalsIgnoreCase("level")) {
            int level = dataManager.getLevel(uuid);
            p.sendMessage("§6=== Your Stats ===");
            p.sendMessage("§aLevel: " + level);
            return true;

        } else if (args[0].equalsIgnoreCase("reset")) {
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
            sendHelp(p);
            return true;
        }
    }

    private void sendHelp(Player p) {
        p.sendMessage("§6=== Lives Plugin Commands ===");
        p.sendMessage("§e/life level §7- Check your current level");
        p.sendMessage("§e/life withdraw §7- Convert 1 level into a Level Item");
        p.sendMessage("§e/life reset §7- Reset your level to 5 (OP only)");
        p.sendMessage("§e/invis §7- Toggle invisibility (Level 9+)");
        p.sendMessage("§e/life help §7- Show this help message");
    }
}
