package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.Recipe;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.gui.RecipeGUI;

import java.util.Collections;
import java.util.UUID;

public class MaceCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final RecipeGUI recipeGUI;

    public MaceCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.recipeGUI = new RecipeGUI(plugin.getItemManager()); // Needs getter if private
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Console support for some commands?
            if (args.length > 0 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("set")
                    || args[0].equalsIgnoreCase("start"))) {
                // Allow console
            } else {
                sender.sendMessage("§cOnly players can use this command (mostly)!");
                return true;
            }
        }

        Player p = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- /might recipe [reset] ---
        if (sub.equals("recipe")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("reset")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                // Remove old custom recipes first
                JavaPlugin javaPlugin = plugin;
                NamespacedKey[] keys = new NamespacedKey[] {
                    new NamespacedKey(javaPlugin, "warden_mace"),
                    new NamespacedKey(javaPlugin, "nether_mace"),
                    new NamespacedKey(javaPlugin, "end_mace"),
                    new NamespacedKey(javaPlugin, "chicken_bow")
                };
                for (NamespacedKey key : keys) {
                    Bukkit.removeRecipe(key);
                }
                // Re-register them
                plugin.getItemManager().registerWardenMaceRecipe();
                plugin.getItemManager().registerNetherMaceRecipe();
                plugin.getItemManager().registerEndMaceRecipe();
                plugin.getItemManager().registerChickenBowRecipe();
                sender.sendMessage("§aCustom recipes re-registered!");
                // Reset the lockout flags so all can be crafted again
                plugin.getDataManager().setWardenMaceCrafted(false);
                plugin.getDataManager().setNetherMaceCrafted(false);
                plugin.getDataManager().setEndMaceCrafted(false);
                return true;
            } else if (p != null) {
                recipeGUI.openMainMenu(p);
            } else {
                sender.sendMessage("§cOnly players can use this command!");
            }
            return true;
        }

        // --- /might give <item> ---
        if (sub.equals("give")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            Player target = (Player) sender;
            if (!target.isOp()) {
                target.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length < 2) {
                target.sendMessage("§cUsage: /might give <warden|nether|end|chickenbow>");
                return true;
            }
            ItemStack weapon = null;
            switch (args[1].toLowerCase()) {
                case "warden":
                    weapon = plugin.getItemManager().createWardenMace();
                    break;
                case "nether":
                    weapon = plugin.getItemManager().createNetherMace();
                    break;
                case "end":
                    weapon = plugin.getItemManager().createEndMace();
                    break;
                case "chickenbow":
                    weapon = plugin.getItemManager().createChickenBow();
                    break;
                default:
                    target.sendMessage("§cUnknown weapon. Usage: /might give <warden|nether|end|chickenbow>");
                    return true;
            }
            if (weapon != null) {
                target.getInventory().addItem(weapon);
                target.sendMessage("§aGiven " + args[1] + " to your inventory.");
            }
            return true;
        }

        // --- /might point set <player> <amount> ---
        if (sub.equals("point")) {
            if (!sender.isOp()) {
                sender.sendMessage("§cYou do not have permission.");
                return true;
            }
            if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                sender.sendMessage("§cUsage: /might point set <player> <amount>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
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

        switch (sub) {
            case "help":
                sendHelp(sender);
                return true;

            case "togglecontrol":
                if (p == null)
                    return true;
                UUID uuid = p.getUniqueId();
                boolean current = plugin.isMaceInteractMode(uuid);
                plugin.setMaceInteractMode(uuid, !current);
                if (!current) {
                    p.sendMessage("§aMace Control Mode enabled! (Right Click / Shift+Right Click)");
                } else {
                    p.sendMessage("§cMace Control Mode disabled! (Use commands)");
                }
                return true;

            case "set":
                // /might set <player> <amount>
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /might set <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
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

            case "withdraw":
                // /might withdraw <amount>
                if (p == null) {
                    sender.sendMessage("§cConsole cannot withdraw.");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§cUsage: /might withdraw <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount <= 0) {
                        p.sendMessage("§cAmount must be positive.");
                        return true;
                    }
                    int currentPoints = dataManager.getPoints(p.getUniqueId());
                    if (currentPoints < amount) {
                        p.sendMessage("§cNot enough Might! You have " + currentPoints + ".");
                        return true;
                    }

                    // Deduct points
                    dataManager.setPoints(p.getUniqueId(), currentPoints - amount);

                    // Give items
                    ItemStack item = new ItemStack(Material.NETHER_STAR, amount);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§6Might Token");
                    meta.setLore(Collections.singletonList("§7Right-click to reclaim 1 Might"));
                    item.setItemMeta(meta);

                    p.getInventory().addItem(item);
                    p.sendMessage("§aWithdrew " + amount + " Might!");
                } catch (NumberFormatException e) {
                    p.sendMessage("§cInvalid number.");
                }
                return true;

            case "reset":
                // /might reset <target|@a>
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /might reset <player|all>");
                    return true;
                }

                String targetStr = args[1];
                if (targetStr.equalsIgnoreCase("all") || targetStr.equalsIgnoreCase("@a")) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        dataManager.setPoints(online.getUniqueId(), 2); // Reset to starting 2
                        online.sendMessage("§eYour Might has been reset to 2 by an admin.");
                    }
                    sender.sendMessage("§aReset Might for all players to 2.");
                } else {
                    Player resetTarget = Bukkit.getPlayer(targetStr);
                    if (resetTarget == null) {
                        sender.sendMessage("§cPlayer not found.");
                        return true;
                    }
                    dataManager.setPoints(resetTarget.getUniqueId(), 2);
                    resetTarget.sendMessage("§eYour Might has been reset to 2 by an admin.");
                    sender.sendMessage("§aReset " + resetTarget.getName() + "'s Might to 2.");
                }
                return true;

            case "start":
                // /might start
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                // Reset Might for all players
                for (Player online : Bukkit.getOnlinePlayers()) {
                    dataManager.setPoints(online.getUniqueId(), 2);
                    online.sendMessage("§eYour Might has been reset to 2 by an admin (event start)!");
                }
                // Reset recipes and unique flags just like recipe reset
                JavaPlugin javaPlugin = plugin;
                NamespacedKey[] keys = new NamespacedKey[] {
                    new NamespacedKey(javaPlugin, "warden_mace"),
                    new NamespacedKey(javaPlugin, "nether_mace"),
                    new NamespacedKey(javaPlugin, "end_mace"),
                    new NamespacedKey(javaPlugin, "chicken_bow")
                };
                for (NamespacedKey key : keys) {
                    Bukkit.removeRecipe(key);
                }
                plugin.getItemManager().registerWardenMaceRecipe();
                plugin.getItemManager().registerNetherMaceRecipe();
                plugin.getItemManager().registerEndMaceRecipe();
                plugin.getItemManager().registerChickenBowRecipe();
                plugin.getDataManager().setWardenMaceCrafted(false);
                plugin.getDataManager().setNetherMaceCrafted(false);
                plugin.getDataManager().setEndMaceCrafted(false);
                sender.sendMessage("§aAll players' Might reset and custom recipes re-registered for the event!");
                // The rest of original start logic (countdown etc.)
                sender.sendMessage("§aStarting 10s countdown to Border Set...");
                Bukkit.broadcastMessage("§c§lBorder will be set to 3000 in 10 seconds!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Bukkit.broadcastMessage("§c§lSetting World Border to 3000!");
                    if (Bukkit.getWorld("world") != null) {
                        Bukkit.getWorld("world").getWorldBorder().setSize(3000);
                        Bukkit.broadcastMessage("§aBorder set!");
                    } else {
                        // Fallback try fallback world
                        if (!Bukkit.getWorlds().isEmpty()) {
                            Bukkit.getWorlds().get(0).getWorldBorder().setSize(3000);
                            Bukkit.broadcastMessage("§aBorder set (default world)!");
                        } else {
                            sender.sendMessage("§cCould not find world to set border.");
                        }
                    }
                }, 200L); // 10 seconds
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Mace & Might Commands ===");
        if (sender instanceof Player) {
            sender.sendMessage("§e/might togglecontrol §7- Toggle click-to-cast");
            sender.sendMessage("§e/might withdraw <amount> §7- Convert Might to item");
            sender.sendMessage("§e/might recipe §7- Show custom recipe menu");
        }
        if (sender.isOp()) {
            sender.sendMessage("§c=== Admin ===");
            sender.sendMessage("§e/might give <item> §7- Get a special item");
            sender.sendMessage("§e/might set <player> <amount> §7- Set Might");
            sender.sendMessage("§e/might reset <player|all> §7- Reset Might to 2");
            sender.sendMessage("§e/might start §7- Start Border Countdown");
            sender.sendMessage("§e/might recipe reset §7- Re-register all custom recipes");
            sender.sendMessage("§e/might point set <player> <amount> §7- Set player Might");
        }
    }
}
