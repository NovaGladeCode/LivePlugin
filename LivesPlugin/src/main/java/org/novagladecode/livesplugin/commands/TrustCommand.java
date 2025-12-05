package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.UUID;

public class TrustCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;

    public TrustCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("trust")) {
            if (args.length != 1) {
                p.sendMessage("§cUsage: /trust <player>");
                return true;
            }

            // Find target player (online or offline)
            Player target = Bukkit.getPlayer(args[0]);
            UUID targetUUID;
            String targetName;

            if (target != null) {
                targetUUID = target.getUniqueId();
                targetName = target.getName();
            } else {
                // Try to find offline player handled by DataManager helper or iterate known
                // players
                // For simplicity, we'll rely on DataManager or Bukkit.getOfflinePlayer if
                // exists
                // We'll use the helper method in DataManager if available, or just offline
                // player
                targetUUID = dataManager.getPlayerUUIDByName(args[0]);
                if (targetUUID == null) {
                    p.sendMessage("§cPlayer not found (must be valid or have played before).");
                    return true;
                }
                targetName = args[0]; // Best guess or fetch offline player name
            }

            if (targetUUID.equals(p.getUniqueId())) {
                p.sendMessage("§cYou cannot trust yourself (redundant).");
                return true;
            }

            dataManager.addTrusted(p.getUniqueId(), targetUUID);
            p.sendMessage("§aYou have trusted " + targetName + ". They will not be affected by your mace abilities.");
            return true;

        } else if (cmd.getName().equalsIgnoreCase("untrust")) {
            if (args.length != 1) {
                p.sendMessage("§cUsage: /untrust <player>");
                return true;
            }

            // Simple lookup logic again
            UUID targetUUID = dataManager.getPlayerUUIDByName(args[0]);
            // Fallback for online player if lookup fails (though lookup iterates all data
            // keys)
            if (targetUUID == null) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null)
                    targetUUID = target.getUniqueId();
            }

            if (targetUUID == null) {
                p.sendMessage("§cPlayer not found or not in your trust list.");
                return true;
            }

            dataManager.removeTrusted(p.getUniqueId(), targetUUID);
            p.sendMessage("§cYou have untrusted " + args[0] + ".");
            return true;
        }

        return false;
    }
}
