package org.novagladecode.livesplugin;

import org.bukkit.plugin.java.JavaPlugin;

import org.novagladecode.livesplugin.commands.NetherMaceCommand;
import org.novagladecode.livesplugin.commands.WardenMaceCommand;
import org.novagladecode.livesplugin.commands.EndMaceCommand;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.ItemManager;

public class LivePlugin extends JavaPlugin {

    private PlayerDataManager dataManager;
    private ItemManager itemManager;
    private WardenMaceCommand wardenMaceCommand;
    private NetherMaceCommand netherMaceCommand;
    private EndMaceCommand endMaceCommand;
    private java.util.HashMap<java.util.UUID, Boolean> maceInteractMode = new java.util.HashMap<>();

    @Override
    public void onEnable() {
        // Initialize Managers
        this.dataManager = new PlayerDataManager(this);
        this.itemManager = new ItemManager(this);
        org.novagladecode.livesplugin.gui.RecipeGUI recipeGUI = new org.novagladecode.livesplugin.gui.RecipeGUI(
                itemManager);
        getServer().getPluginManager().registerEvents(recipeGUI, this);

        // Register Commands
        // Register Commands
        wardenMaceCommand = new WardenMaceCommand(this, dataManager);
        getCommand("wardenmace").setExecutor(wardenMaceCommand);

        netherMaceCommand = new org.novagladecode.livesplugin.commands.NetherMaceCommand(this, dataManager);
        getCommand("nethermace").setExecutor(netherMaceCommand);

        endMaceCommand = new org.novagladecode.livesplugin.commands.EndMaceCommand(this, dataManager);
        getCommand("endmace").setExecutor(endMaceCommand);

        getCommand("might").setExecutor(new org.novagladecode.livesplugin.commands.MaceCommand(this, dataManager));

        getCommand("weapon")
                .setExecutor(
                        new org.novagladecode.livesplugin.commands.WeaponCommand(itemManager, recipeGUI, dataManager));

        getCommand("trust").setExecutor(new org.novagladecode.livesplugin.commands.TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new org.novagladecode.livesplugin.commands.TrustCommand(dataManager));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new org.novagladecode.livesplugin.listeners.GameListener(this,
                dataManager, itemManager), this);
        getServer().getPluginManager().registerEvents(new org.novagladecode.livesplugin.listeners.MaceListener(this,
                wardenMaceCommand, netherMaceCommand, endMaceCommand), this);

        // Register Recipes (Duplicates removed, kept one set)
        itemManager.registerWardenMaceRecipe();
        itemManager.registerNetherMaceRecipe();
        itemManager.registerEndMaceRecipe();
        itemManager.registerChickenBowRecipe();

        getLogger().info("Lives Plugin has been enabled! (Lite Mode)");
    }

    public boolean isMaceInteractMode(java.util.UUID uuid) {
        return maceInteractMode.getOrDefault(uuid, false);
    }

    public void setMaceInteractMode(java.util.UUID uuid, boolean enabled) {
        maceInteractMode.put(uuid, enabled);
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("Lives Plugin has been disabled!");
    }
}
