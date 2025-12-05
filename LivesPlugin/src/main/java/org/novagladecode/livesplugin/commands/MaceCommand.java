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

        if (args.length >= 2 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("nether")) {
            ItemStack mace = new ItemStack(Material.MACE);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName("§cNether Mace");
            List<String> lore = new ArrayList<>();
            lore.add("§7Forged in the depths of the Nether...");
            lore.add("§7Abilities:");
            lore.add("§6/nethermace 1 §7- Infernal Wrath");
            lore.add("§6/nethermace 2 §7- Fire Tornado");
            meta.setLore(lore);
            mace.setItemMeta(meta);

            p.getInventory().addItem(mace);
            p.sendMessage("§aGiven Nether Mace!");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("chicken")) {
            ItemStack mace = new ItemStack(Material.MACE);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName("§eChicken Mace");
            List<String> lore = new ArrayList<>();
            lore.add("§7The power of poultry...");
            lore.add("§7Passive: §6No Fall Damage");
            lore.add("§7Abilities:");
            lore.add("§e/chickenmace 1 §7- Chicken Army");
            meta.setLore(lore);
            mace.setItemMeta(meta);

            p.getInventory().addItem(mace);
            p.sendMessage("§aGiven Chicken Mace!");
            return true;
        }

        p.sendMessage("§cUsage: /mace give <warden|nether|chicken>");
        return true;
    }
}
