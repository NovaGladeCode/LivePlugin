package org.novagladecode.livesplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.novagladecode.livesplugin.logic.ItemManager;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class RecipeGUI implements Listener {

    private final ItemManager itemManager;

    public RecipeGUI(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, "§8Custom Recipes");

        inv.setItem(0, createNamedGuiItem(itemManager.createWardenMace(), "§3Warden Mace Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(1, createNamedGuiItem(itemManager.createNetherMace(), "§cNether Mace Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(2, createNamedGuiItem(itemManager.createEndMace(), "§5End Mace Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(3, createNamedGuiItem(itemManager.createChickenBow(), "§eChicken Bow Recipe",
                "§7Click to view usage & crafting"));
        // inv.setItem(4, createNamedGuiItem(itemManager.createUnbanItem(), "§5Unban Token Recipe", "§7Click to view usage & crafting")); // Removed from GUI

        p.openInventory(inv);
    }

    public void openRecipeView(Player p, String itemType) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Recipe: " + itemType);

        // Fill background
        ItemStack filler = createNamedGuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " ");
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, filler);
        }

        // 3x3 Grid Slots in a 9-row inventory (using Chest 5x9 for space)
        // Center the 3x3 grid around slots: 11,12,13, 20,21,22, 29,30,31
        int[] gridSlots = { 11, 12, 13, 20, 21, 22, 29, 30, 31 };
        int resultSlot = 24; // Result on the right

        ItemStack[] ingredients = new ItemStack[9];
        ItemStack result = null;

        Material D_BLOCK = Material.DIAMOND_BLOCK;
        Material DIAMOND = Material.DIAMOND;
        Material HEAVY_CORE = Material.HEAVY_CORE;
        Material N_INGOT = Material.NETHERITE_INGOT;
        Material N_SCRAP = Material.NETHERITE_SCRAP;
        Material CHORUS = Material.POPPED_CHORUS_FRUIT;
        Material BREEZE = Material.BREEZE_ROD;
        Material FEATHER = Material.FEATHER;
        Material BOW = Material.BOW;
        Material BLAZE = Material.BLAZE_ROD;

        switch (itemType) {
            case "Warden Mace":
                // C S C
                // H M H
                // C S C
                ingredients[0] = new ItemStack(Material.SCULK_CATALYST);
                ingredients[1] = new ItemStack(Material.SCULK_SENSOR);
                ingredients[2] = new ItemStack(Material.SCULK_CATALYST);
                ingredients[3] = itemManager.createWardenHeart();
                ingredients[4] = new ItemStack(HEAVY_CORE);
                ingredients[5] = itemManager.createWardenHeart();
                ingredients[6] = new ItemStack(Material.SCULK_CATALYST);
                ingredients[7] = new ItemStack(Material.SCULK_SENSOR);
                ingredients[8] = new ItemStack(Material.SCULK_CATALYST);
                result = itemManager.createWardenMace();
                break;
            case "Nether Mace":
                // I S I
                // B M W
                // S S S
                ingredients[0] = new ItemStack(N_INGOT);
                ingredients[1] = new ItemStack(N_SCRAP);
                ingredients[2] = new ItemStack(N_INGOT);
                ingredients[3] = new ItemStack(BLAZE);
                ingredients[4] = new ItemStack(HEAVY_CORE);
                ingredients[5] = itemManager.createWitherHeart();
                ingredients[6] = new ItemStack(N_SCRAP);
                ingredients[7] = new ItemStack(N_SCRAP);
                ingredients[8] = new ItemStack(N_SCRAP);
                result = itemManager.createNetherMace();
                break;
            case "End Mace":
                // P H P
                // H M H
                // P B P
                ingredients[0] = new ItemStack(CHORUS);
                ingredients[1] = itemManager.createDragonHeart();
                ingredients[2] = new ItemStack(CHORUS);
                ingredients[3] = itemManager.createDragonHeart();
                ingredients[4] = new ItemStack(HEAVY_CORE);
                ingredients[5] = itemManager.createDragonHeart();
                ingredients[6] = new ItemStack(CHORUS);
                ingredients[7] = new ItemStack(BREEZE);
                ingredients[8] = new ItemStack(CHORUS);
                result = itemManager.createEndMace();
                break;
            case "Chicken Bow":
                // D F D
                // F B F
                // D F D
                ingredients[0] = new ItemStack(DIAMOND);
                ingredients[1] = new ItemStack(FEATHER);
                ingredients[2] = new ItemStack(DIAMOND);
                ingredients[3] = new ItemStack(FEATHER);
                ingredients[4] = new ItemStack(BOW);
                ingredients[5] = new ItemStack(FEATHER);
                ingredients[6] = new ItemStack(DIAMOND);
                ingredients[7] = new ItemStack(FEATHER);
                ingredients[8] = new ItemStack(DIAMOND);
                result = itemManager.createChickenBow();
                break;
            case "Unban Token":
                // (do nothing -- unban token removed)
                break;
        }

        // Place items
        for (int i = 0; i < 9; i++) {
            if (ingredients[i] != null) {
                inv.setItem(gridSlots[i], ingredients[i]);
            }
        }
        if (result != null) {
            inv.setItem(resultSlot, result);
        }

        // Back Button
        inv.setItem(40, createNamedGuiItem(new ItemStack(Material.ARROW), "§cBack", "§7Return to menu"));

        p.openInventory(inv);
    }

    private ItemStack createNamedGuiItem(ItemStack base, String name, String... lore) {
        ItemStack item = base.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        String title = e.getView().getTitle();

        if (title.equals("§8Custom Recipes")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack current = e.getCurrentItem();
            if (current == null || !current.hasItemMeta())
                return;

            String name = current.getItemMeta().getDisplayName();
            if (name.contains("Warden Mace"))
                openRecipeView(p, "Warden Mace");
            else if (name.contains("Nether Mace"))
                openRecipeView(p, "Nether Mace");
            else if (name.contains("End Mace"))
                openRecipeView(p, "End Mace");
            else if (name.contains("Chicken Bow"))
                openRecipeView(p, "Chicken Bow");
            else if (name.contains("Unban Token"))
                openRecipeView(p, "Unban Token");
        } else if (title.startsWith("§8Recipe: ")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack current = e.getCurrentItem();
            if (current != null && current.getType() == Material.ARROW && current.hasItemMeta()
                    && current.getItemMeta().getDisplayName().equals("§cBack")) {
                openMainMenu(p);
            }
        }
    }
}
