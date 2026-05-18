package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.necromancer.NecromancerManager;
import org.novagladecode.livesplugin.warlord.WarlordManager;

import java.util.Arrays;

/**
 * Handles: /skills admin spawn <boss>
 *          /skills admin givespawner warlord
 *          /spawnboss <boss>        (alias for convenience)
 */
public class SkillsAdminCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final WarlordManager warlordManager;
    private final NecromancerManager necromancerManager;

    // PersistentDataKey for spawner item
    private static final String SPAWNER_ITEM_KEY = "warlord_spawner_item";

    public SkillsAdminCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.warlordManager    = WarlordManager.getInstance(plugin);
        this.necromancerManager = NecromancerManager.getInstance(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // /spawnboss necromancer  (direct alias)
        if (label.equalsIgnoreCase("spawnboss")) {
            if (!sender.isOp()) { sender.sendMessage("§cNo permission."); return true; }
            if (args.length == 0) { sender.sendMessage("§cUsage: /spawnboss <warlord|necromancer>"); return true; }
            handleSpawn(sender, args[0]);
            return true;
        }

        // /skills admin <subcommand> [args...]
        if (args.length < 2 || !args[0].equalsIgnoreCase("admin")) {
            sender.sendMessage("§6=== Skills Admin Commands ===");
            sender.sendMessage("§e/skills admin spawn <warlord|necromancer>");
            sender.sendMessage("§e/skills admin givespawner warlord");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage("§cYou need admin (OP) to use this command.");
            return true;
        }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "spawn" -> {
                if (args.length < 3) { sender.sendMessage("§cUsage: /skills admin spawn <warlord|necromancer>"); return true; }
                handleSpawn(sender, args[2]);
            }
            case "givespawner" -> {
                if (args.length < 3 || !args[2].equalsIgnoreCase("warlord")) {
                    sender.sendMessage("§cUsage: /skills admin givespawner warlord");
                    return true;
                }
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("§cOnly players can receive items."); return true;
                }
                giveWarlordSpawnerItem(p);
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand. Try: spawn, givespawner");
            }
        }
        return true;
    }

    private void handleSpawn(CommandSender sender, String boss) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cOnly players can use this (must be at a location).");
            return;
        }
        switch (boss.toLowerCase()) {
            case "warlord" -> {
                if (warlordManager.getPhase() != WarlordManager.Phase.INACTIVE) {
                    sender.sendMessage("§cThe Warlord fight is already active!");
                    return;
                }
                boolean started = warlordManager.startFight(p.getLocation());
                if (!started) sender.sendMessage("§cCould not start fight.");
            }
            case "necromancer" -> {
                if (necromancerManager.isActive()) {
                    sender.sendMessage("§cThe Necromancer is already active!");
                    return;
                }
                necromancerManager.spawnWithAnimation(p.getLocation());
            }
            default -> sender.sendMessage("§cUnknown boss: " + boss + ". Use: warlord, necromancer");
        }
    }

    private void giveWarlordSpawnerItem(Player p) {
        // Use a Slime Spawn Egg as the spawner placer item
        ItemStack item = new ItemStack(Material.SLIME_SPAWN_EGG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lWarlord Spawner");
        meta.setLore(Arrays.asList(
                "§8━━━━━━━━━━━━━━━━━━━━━━━",
                "§7Place this during Phase 2 of",
                "§7the §c§lWarlord §7fight to deploy",
                "§7an additional Warlord Spawner.",
                "§8━━━━━━━━━━━━━━━━━━━━━━━",
                "§c§lADMIN ITEM"
        ));
        NamespacedKey key = new NamespacedKey(plugin, SPAWNER_ITEM_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        p.getInventory().addItem(item);
        p.sendMessage("§aYou received the §lWarlord Spawner §aitem!");
    }

    /** Check if an item is the Warlord Spawner placer. */
    public boolean isSpawnerItem(ItemStack item) {
        if (item == null || item.getType() != Material.SLIME_SPAWN_EGG || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, SPAWNER_ITEM_KEY);
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
