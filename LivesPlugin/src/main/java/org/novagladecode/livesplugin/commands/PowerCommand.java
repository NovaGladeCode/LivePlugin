package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.PowerType;

import java.util.Random;

public class PowerCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final Random random = new Random();

    public PowerCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cYou do not have permission to use this command!");
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("roll")) {
            sender.sendMessage("§cUsage: /power roll [player]");
            return true;
        }

        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }
        } else {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("§cConsole must specify a player!");
                return true;
            }
        }

        // Roll a random power (excluding NONE)
        PowerType[] powers = PowerType.values();
        PowerType newPower;
        do {
            newPower = powers[random.nextInt(powers.length)];
        } while (newPower == PowerType.NONE);

        dataManager.setPower(target.getUniqueId(), newPower.name());
        dataManager.saveData();

        String powerName = newPower.name().charAt(0) + newPower.name().substring(1).toLowerCase();
        target.sendMessage("§aYou have been granted the power of: §e" + powerName + "§a!");
        if (!target.equals(sender)) {
            sender.sendMessage("§aGranted power §e" + powerName + " §ato " + target.getName());
        }

        return true;
    }
}
