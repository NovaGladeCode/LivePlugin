package org.novagladecode.livesplugin.warlord;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

/** Updates each player's compass to point at their assigned spawner every tick cycle. */
public class WarlordCompass extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final Map<UUID, WarlordSpawner> assignments; // playerUUID → assigned spawner

    public WarlordCompass(JavaPlugin plugin, Map<UUID, WarlordSpawner> assignments) {
        this.plugin      = plugin;
        this.assignments = assignments;
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, WarlordSpawner> entry : assignments.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            WarlordSpawner spawner = entry.getValue();

            if (p == null || !p.isOnline()) continue;

            if (spawner == null || !spawner.isAlive() || spawner.getLocation() == null) {
                // Find a new alive spawner
                WarlordSpawner next = assignments.values().stream()
                        .filter(s -> s != null && s.isAlive())
                        .findFirst().orElse(null);
                assignments.put(entry.getKey(), next);
                if (next == null) {
                    p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            new net.md_5.bungee.api.chat.TextComponent("§aAll spawners destroyed!"));
                    continue;
                }
                spawner = next;
            }

            Location target = spawner.getLocation();
            if (target == null) continue;

            // Update compass target for the player
            p.setCompassTarget(target);

            // Update action bar
            double dist = Math.round(p.getLocation().distance(target) * 10.0) / 10.0;
            String bar = "§6§l⚠ SPAWNER §r§7» §e" + dist + " §7blocks away §8| §cDestroy it!";
            p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(bar));

            // Ensure player has the tracker compass
            ensureCompass(p);
        }
    }

    private void ensureCompass(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS
                    && item.hasItemMeta()
                    && "§6Spawner Tracker".equals(item.getItemMeta().getDisplayName())) {
                return; // already has it
            }
        }
        // Give compass
        ItemStack compass = new ItemStack(Material.COMPASS);
        var meta = compass.getItemMeta();
        meta.setDisplayName("§6Spawner Tracker");
        meta.setLore(java.util.Arrays.asList("§7Points toward your assigned", "§7Warlord Spawner."));
        compass.setItemMeta(meta);
        p.getInventory().addItem(compass);
        p.sendMessage("§6You received a §lSpawner Tracker§r§6 compass!");
    }

    /** Remove the tracker compass from a player when the fight ends. */
    public static void removeCompass(Player p) {
        p.getInventory().getContents();
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item != null && item.getType() == Material.COMPASS
                    && item.hasItemMeta()
                    && "§6Spawner Tracker".equals(item.getItemMeta().getDisplayName())) {
                p.getInventory().setItem(i, null);
            }
        }
    }
}
