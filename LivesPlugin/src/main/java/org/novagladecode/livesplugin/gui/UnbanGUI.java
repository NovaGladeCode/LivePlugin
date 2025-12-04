package org.novagladecode.livesplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnbanGUI implements Listener {

    private final PlayerDataManager dataManager;
    private static final String GUI_TITLE = "§5Select Player to Unban";

    public UnbanGUI(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void openUnbanMenu(Player player) {
        List<UUID> bannedPlayers = dataManager.getBannedPlayers();

        if (bannedPlayers.isEmpty()) {
            player.sendMessage("§cNo players are currently banned!");
            return;
        }

        int size = Math.min(54, ((bannedPlayers.size() + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, size, GUI_TITLE);

        for (int i = 0; i < bannedPlayers.size() && i < 54; i++) {
            UUID uuid = bannedPlayers.get(i);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            meta.setDisplayName("§e" + Bukkit.getOfflinePlayer(uuid).getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to unban this player");
            meta.setLore(lore);

            skull.setItemMeta(meta);
            inv.setItem(i, skull);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        if (!e.getView().getTitle().equals(GUI_TITLE))
            return;

        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD)
            return;

        SkullMeta meta = (SkullMeta) clicked.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null)
            return;

        UUID targetUUID = meta.getOwningPlayer().getUniqueId();
        String targetName = meta.getOwningPlayer().getName();

        dataManager.setBanned(targetUUID, false);
        dataManager.saveData();

        p.closeInventory();
        p.sendMessage("§aYou have unbanned " + targetName + "!");
    }
}
