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
            if (p != null) {
                int points = dataManager.getPoints(p.getUniqueId());
                p.sendMessage("§6§lForge Details:");
                p.sendMessage("§eCurrent Forge Level: §b" + points);
                p.sendMessage("§7Use /forge help to see all commands.");
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- /forge recipe [reset] ---
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

        if (sub.equals("give")) {
            if (!sender.isOp()) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /forge give <item> [player]");
                return true;
            }

            String itemName = args[1];
            Player target = p;

            if (args.length >= 3) {
                Player potentialTarget = Bukkit.getPlayer(args[1]);
                if (potentialTarget != null) {
                    target = potentialTarget;
                    itemName = args[2].toLowerCase();
                } else {
                    potentialTarget = Bukkit.getPlayer(args[2]);
                    if (potentialTarget != null) {
                        target = potentialTarget;
                        itemName = args[1].toLowerCase();
                    } else {
                        sender.sendMessage("§cPlayer " + args[1] + " or " + args[2] + " not found.");
                        return true;
                    }
                }
            }

            if (target == null) {
                sender.sendMessage("§cConsole must specify a player: /forge give <item> <player>");
                return true;
            }

            ItemStack weapon = null;
            switch (itemName.toLowerCase()) {
                case "warden":
                case "wardenmace":
                    weapon = plugin.getItemManager().createWardenMace();
                    break;
                case "nether":
                case "nethermace":
                    weapon = plugin.getItemManager().createNetherMace();
                    break;
                case "end":
                case "endmace":
                    weapon = plugin.getItemManager().createEndMace();
                    break;
                case "chickenbow":
                    weapon = plugin.getItemManager().createChickenBow();
                    break;
                case "ghostblade":
                    weapon = plugin.getItemManager().createGhostblade();
                    break;
                case "dragonblade":
                    weapon = plugin.getItemManager().createDragonblade();
                    break;
                case "mistblade":
                    weapon = plugin.getItemManager().createMistblade();
                    break;
                case "soulblade":
                    weapon = plugin.getItemManager().createSoulblade();
                    break;

                case "wardenheart":
                    weapon = plugin.getItemManager().createWardenHeart();
                    break;
                case "witherheart":
                    weapon = plugin.getItemManager().createWitherHeart();
                    break;
                case "dragonheart":
                    weapon = plugin.getItemManager().createDragonHeart();
                    break;
                case "unban":
                    weapon = plugin.getItemManager().createUnbanItem();
                    break;
                case "level":
                    weapon = plugin.getItemManager().createLevelItem();
                    break;
                case "wardenforge":
                    weapon = plugin.getItemManager().createSacredForge("warden");
                    break;
                case "netherforge":
                    weapon = plugin.getItemManager().createSacredForge("nether");
                    break;
                case "endforge":
                    weapon = plugin.getItemManager().createSacredForge("end");
                    break;
                case "ghostbladeforge":
                    weapon = plugin.getItemManager().createSacredForge("ghostblade");
                    break;
                case "dragonbladeforge":
                    weapon = plugin.getItemManager().createSacredForge("dragonblade");
                    break;
                case "mistbladeforge":
                    weapon = plugin.getItemManager().createSacredForge("mistblade");
                    break;
                case "soulbladeforge":
                    weapon = plugin.getItemManager().createSacredForge("soulblade");
                    break;
                case "allforges":
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("warden"));
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("nether"));
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("end"));
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("ghostblade"));
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("dragonblade"));
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("mistblade"));
                    target.getInventory().addItem(plugin.getItemManager().createSacredForge("soulblade"));
                    target.sendMessage("§aYou have been given one of every Sacred Forge type!");
                    if (sender != target) {
                        sender.sendMessage("§aGiven all Sacred Forge types to " + target.getName() + ".");
                    }
                    return true;
                default:
                    sender.sendMessage(
                            "§cUnknown item. Try: warden, nether, end, ghostblade, dragonblade, mistblade, soulblade, chickenbow, hearts, unban, level, [type]forge, allforges");
                    return true;
            }
            if (weapon != null) {
                target.getInventory().addItem(weapon);
                target.sendMessage("§aGiven " + itemName + " to your inventory.");
                if (sender != target) {
                    sender.sendMessage("§aGiven " + itemName + " to " + target.getName() + ".");
                }
            }
            return true;
        }

        // --- /forge point set <player> <amount> ---
        if (sub.equals("point")) {
            if (!sender.isOp()) {
                sender.sendMessage("§cYou do not have permission.");
                return true;
            }
            if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                sender.sendMessage("§cUsage: /forge point set <player> <amount>");
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
                sender.sendMessage("§aSet " + target.getName() + "'s Forge Level to " + amount);
                target.sendMessage("§aYour Forge Level was set to " + amount + " by an admin.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number.");
            }
            return true;
        }
        if (sub.equals("level")) {
            if (p != null) {
                int points = dataManager.getPoints(p.getUniqueId());
                p.sendMessage("§eYour current Forge Level is: §b" + points);
            } else {
                sender.sendMessage("§cOnly players have a Forge Level.");
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
                if (!sender.isOp() || p == null) {
                    sender.sendMessage("§cOnly OPs can use this, and must be a player.");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§cUsage: /forge set <type>");
                    p.sendMessage("§7Types: warden, nether, end, ghostblade, dragonblade, mistblade, soulblade");
                    return true;
                }
                String type = args[1].toLowerCase();
                ItemStack forgeItem = plugin.getItemManager().createSacredForge(type);
                if (forgeItem == null) {
                    p.sendMessage("§cInvalid forge type.");
                    return true;
                }
                p.getInventory().addItem(forgeItem);
                p.sendMessage("§aYou have been given a §b" + type + " Forge§a!");
                return true;

            case "toggle":
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /forge toggle <name>");
                    return true;
                }
                String name = args[1].toLowerCase();
                boolean newState = !plugin.isAbilityEnabled(name);
                plugin.setAbilityEnabled(name, newState);
                String stateStr = newState ? "§aENABLED" : "§cDISABLED";
                Bukkit.broadcastMessage(
                        "§6§lForge Toggled: §b" + name + " §7is now " + stateStr + " §7for all players!");
                return true;

            case "withdraw":
                // /forge withdraw <amount>
                if (p == null) {
                    sender.sendMessage("§cConsole cannot withdraw.");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§cUsage: /forge withdraw <amount>");
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
                        p.sendMessage("§cNot enough Forge Points! You have " + currentPoints + ".");
                        return true;
                    }

                    // Deduct points
                    dataManager.setPoints(p.getUniqueId(), currentPoints - amount);

                    // Give items
                    ItemStack item = new ItemStack(Material.NETHER_STAR, amount);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§6Forge Token");
                    meta.setLore(Collections.singletonList("§7Right-click to reclaim 1 Forge Point"));
                    item.setItemMeta(meta);

                    p.getInventory().addItem(item);
                    p.sendMessage("§aWithdrew " + amount + " Forge Points!");
                } catch (NumberFormatException e) {
                    p.sendMessage("§cInvalid number.");
                }
                return true;

            case "reset":
                // /forge reset <target|@a>
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /forge reset <player|all>");
                    return true;
                }

                String targetStr = args[1];
                if (targetStr.equalsIgnoreCase("all") || targetStr.equalsIgnoreCase("@a")) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        dataManager.setPoints(online.getUniqueId(), 2); // Reset to starting 2
                        online.sendMessage("§eYour Forge Level has been reset to 2 by an admin.");
                    }
                    sender.sendMessage("§aReset Forge Level for all players to 2.");
                } else {
                    Player resetTarget = Bukkit.getPlayer(targetStr);
                    if (resetTarget == null) {
                        sender.sendMessage("§cPlayer not found.");
                        return true;
                    }
                    dataManager.setPoints(resetTarget.getUniqueId(), 2);
                    resetTarget.sendMessage("§eYour Forge Level has been reset to 2 by an admin.");
                    sender.sendMessage("§aReset " + resetTarget.getName() + "'s Forge Level to 2.");
                }
                return true;

            case "start":
                // /forge start
                if (!sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                // Reset Forge for all players
                for (Player online : Bukkit.getOnlinePlayers()) {
                    dataManager.setPoints(online.getUniqueId(), 2);
                    online.sendMessage("§eYour Forge Level has been reset to 2 by an admin (event start)!");
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
                plugin.getDataManager().setEndMaceCrafted(false);
                plugin.setForgeActive(true);
                sender.sendMessage(
                        "§aAll players' Forge Level reset, custom recipes re-registered, and SACRED FORGE ACTIVATED!");
                Bukkit.broadcastMessage(
                        "§6§lThe Sacred Forge has been ignited! Rituals are now possible at local Sacred Forges.");
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
        sender.sendMessage("§6=== Forgebound SMP Commands ===");
        if (sender instanceof Player) {
            sender.sendMessage("§e/forge level §7- Check your current Forge Level");
            sender.sendMessage("§e/forge togglecontrol §7- Toggle click-to-cast");
            sender.sendMessage("§e/forge withdraw <amount> §7- Convert Forge Points to items");
            sender.sendMessage("§e/forge recipe §7- Show custom recipe menu");
        }
        // Admin section - only visible to operators
        if (sender.isOp()) {
            sender.sendMessage("§6§lForgebound SMP Admin Commands:");
            sender.sendMessage("§e/forge set <type> §7- Get a specialized Forge item");
            sender.sendMessage("§e/forge start §7- Start the event (Set Border + Prep Forges)");
            sender.sendMessage("§e/forge point set <player> <amount> §7- Set Forge Level");
            sender.sendMessage("§e/forge give <player> <item> §7- Give custom items");
            sender.sendMessage("§e/forge toggle <ability> §7- Global ability switch");
            sender.sendMessage("§e/forge reset <player|all> §7- Reset Forge Level to 2");
            sender.sendMessage("§e/forge recipe reset §7- Re-register all custom recipes");
        }
    }
}
