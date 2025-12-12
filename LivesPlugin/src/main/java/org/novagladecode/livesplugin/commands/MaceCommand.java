package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MaceCommand implements CommandExecutor {

    private final org.novagladecode.livesplugin.LivePlugin plugin;

    public MaceCommand(org.novagladecode.livesplugin.LivePlugin plugin) {
        this.plugin = plugin;
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

        } else if (args[0].equalsIgnoreCase("togglecontrol")) {
            boolean current = plugin.isMaceInteractMode(uuid);
            plugin.setMaceInteractMode(uuid, !current);
            if (!current) {
                p.sendMessage("§aMace Control Mode enabled! (Right Click / Shift+Right Click)");
            } else {
                p.sendMessage("§cMace Control Mode disabled! (Use commands)");
            }
            return true;

        } else {
            sendHelp(p);
            return true;
        }
    }

    private void sendHelp(Player p) {
        p.sendMessage("§6=== Mace & Might Commands ===");
        p.sendMessage("§e/might togglecontrol §7- Toggle click-to-cast for maces");
        if (p.isOp()) {
            p.sendMessage("§e/weapon give <warden|nether|end|chickenbow> §7- Get Items");
        }
        p.sendMessage("§6=== Mace Abilities ===");
        p.sendMessage("§bWarden Mace: §7/wardenmace 1 (Sonic Wave), /wardenmace 2 (Grasp)");
        p.sendMessage("§cNether Mace: §7/nethermace 1 (Wrath), /nethermace 2 (Tornado)");
        p.sendMessage("§5End Mace:    §7/endmace 1 (Void Cloak), /endmace 2 (Singularity)");
    }
}
