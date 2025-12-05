package org.novagladecode.livesplugin.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MaceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        if (!p.isOp()) {
            p.sendMessage("§cYou must be an operator to use this command!");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("warden")) {
            ItemStack mace = new ItemStack(Material.MACE);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName("§3Warden Mace");
            List<String> lore = new ArrayList<>();
            lore.add("§7The power of the deep dark...");
            lore.add("§7Abilities:");
            lore.add("§b/wardenmace 1 §7- Sculk Resonance");
            lore.add("§b/wardenmace 2 §7- Sonic Shockwave");
            meta.setLore(lore);
            mace.setItemMeta(meta);

            p.getInventory().addItem(mace);
            p.sendMessage("§aGiven Warden Mace!");
            return true;
        }

        p.sendMessage("§cUsage: /mace give warden");
        return true;
    }
}
