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
        Inventory inv = Bukkit.createInventory(null, 18, "§8Custom Recipes");

        inv.setItem(0, createNamedGuiItem(itemManager.createWardenMace(), "§3Warden Mace Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(1, createNamedGuiItem(itemManager.createNetherMace(), "§cNether Mace Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(2, createNamedGuiItem(itemManager.createEndMace(), "§5End Mace Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(3, createNamedGuiItem(itemManager.createChickenBow(), "§eChicken Bow Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(4, createNamedGuiItem(itemManager.createGhostblade(), "§7Ghostblade Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(5, createNamedGuiItem(itemManager.createDragonblade(), "§6Dragonblade Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(6, createNamedGuiItem(itemManager.createMistblade(), "§bMistblade Recipe",
                "§7Click to view usage & crafting"));
        inv.setItem(7, createNamedGuiItem(itemManager.createSoulblade(), "§8Soulblade Recipe",
                "§7Click to view usage & crafting"));

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
                // S H S
                // C M C
                ingredients[0] = new ItemStack(Material.SCULK_CATALYST);
                ingredients[1] = new ItemStack(Material.SCULK_SENSOR);
                ingredients[2] = new ItemStack(Material.SCULK_CATALYST);
                ingredients[3] = new ItemStack(Material.SCULK_SENSOR);
                ingredients[4] = itemManager.createWardenHeart();
                ingredients[5] = new ItemStack(Material.SCULK_SENSOR);
                ingredients[6] = new ItemStack(Material.SCULK_CATALYST);
                ingredients[7] = new ItemStack(HEAVY_CORE);
                ingredients[8] = new ItemStack(Material.SCULK_CATALYST);
                result = itemManager.createWardenMace();
                break;
            case "Nether Mace":
                // I S I
                // S H S
                // B M B
                ingredients[0] = new ItemStack(N_INGOT);
                ingredients[1] = new ItemStack(N_SCRAP);
                ingredients[2] = new ItemStack(N_INGOT);
                ingredients[3] = new ItemStack(N_SCRAP);
                ingredients[4] = itemManager.createWitherHeart();
                ingredients[5] = new ItemStack(N_SCRAP);
                ingredients[6] = new ItemStack(BLAZE);
                ingredients[7] = new ItemStack(HEAVY_CORE);
                ingredients[8] = new ItemStack(BLAZE);
                result = itemManager.createNetherMace();
                break;
            case "End Mace":
                // P C P
                // C H C
                // B M B
                ingredients[0] = new ItemStack(CHORUS);
                ingredients[1] = new ItemStack(Material.DRAGON_HEAD);
                ingredients[2] = new ItemStack(CHORUS);
                ingredients[3] = new ItemStack(Material.DRAGON_HEAD);
                ingredients[4] = itemManager.createDragonHeart();
                ingredients[5] = new ItemStack(Material.DRAGON_HEAD);
                ingredients[6] = new ItemStack(BREEZE);
                ingredients[7] = new ItemStack(HEAVY_CORE);
                ingredients[8] = new ItemStack(BREEZE);
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
            case "Ghostblade":
                // T N S
                // S W S
                // S N T
                ingredients[0] = new ItemStack(Material.TOTEM_OF_UNDYING);
                ingredients[1] = new ItemStack(Material.NETHERITE_INGOT);
                ingredients[2] = new ItemStack(Material.SOUL_SAND);
                ingredients[3] = new ItemStack(Material.SOUL_SAND);
                ingredients[4] = new ItemStack(Material.NETHERITE_SWORD);
                ingredients[5] = new ItemStack(Material.SOUL_SAND);
                ingredients[6] = new ItemStack(Material.SOUL_SAND);
                ingredients[7] = new ItemStack(Material.NETHERITE_INGOT);
                ingredients[8] = new ItemStack(Material.TOTEM_OF_UNDYING);
                result = itemManager.createGhostblade();
                break;
            case "Dragonblade":
                // H B H
                // B W B
                // H B H
                ingredients[0] = new ItemStack(Material.DRAGON_HEAD);
                ingredients[1] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[2] = new ItemStack(Material.DRAGON_HEAD);
                ingredients[3] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[4] = new ItemStack(Material.NETHERITE_SWORD);
                ingredients[5] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[6] = new ItemStack(Material.DRAGON_HEAD);
                ingredients[7] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[8] = new ItemStack(Material.DRAGON_HEAD);
                result = itemManager.createDragonblade();
                break;
            case "Mistblade":
                // T B T
                // B W B
                // T B T
                ingredients[0] = new ItemStack(Material.TRIDENT);
                ingredients[1] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[2] = new ItemStack(Material.TRIDENT);
                ingredients[3] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[4] = new ItemStack(Material.NETHERITE_SWORD);
                ingredients[5] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[6] = new ItemStack(Material.TRIDENT);
                ingredients[7] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[8] = new ItemStack(Material.TRIDENT);
                result = itemManager.createMistblade();
                break;
            case "Soulblade":
                // X N B
                // N S W
                // B N X
                ingredients[0] = new ItemStack(Material.WITHER_SKELETON_SKULL);
                ingredients[1] = new ItemStack(Material.NETHERITE_INGOT);
                ingredients[2] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[3] = new ItemStack(Material.NETHERITE_INGOT);
                ingredients[4] = new ItemStack(Material.NETHERITE_SWORD);
                ingredients[5] = new ItemStack(Material.WITHER_SKELETON_SKULL);
                ingredients[6] = new ItemStack(Material.NETHERITE_BLOCK);
                ingredients[7] = new ItemStack(Material.NETHERITE_INGOT);
                ingredients[8] = new ItemStack(Material.WITHER_SKELETON_SKULL);
                result = itemManager.createSoulblade();
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
            if (name != null && name.contains("Warden Mace"))
                openRecipeView(p, "Warden Mace");
            else if (name != null && name.contains("Nether Mace"))
                openRecipeView(p, "Nether Mace");
            else if (name != null && name.contains("End Mace"))
                openRecipeView(p, "End Mace");
            else if (name != null && name.contains("Chicken Bow"))
                openRecipeView(p, "Chicken Bow");
            else if (name != null && name.contains("Ghostblade"))
                openRecipeView(p, "Ghostblade");
            else if (name != null && name.contains("Dragonblade"))
                openRecipeView(p, "Dragonblade");
            else if (name != null && name.contains("Mistblade"))
                openRecipeView(p, "Mistblade");
            else if (name != null && name.contains("Soulblade"))
                openRecipeView(p, "Soulblade");
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
