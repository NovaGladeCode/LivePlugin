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
    private java.util.HashMap<String, Boolean> globalAbilityToggles = new java.util.HashMap<>();
    private boolean forgeActive = false;

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

        getCommand("forge").setExecutor(new org.novagladecode.livesplugin.commands.MaceCommand(this, dataManager));

        // getCommand("weapon").setExecutor(new
        // org.novagladecode.livesplugin.commands.WeaponCommand(itemManager, recipeGUI,
        // dataManager)); // Weapon command is now obsolete

        getCommand("trust").setExecutor(new org.novagladecode.livesplugin.commands.TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new org.novagladecode.livesplugin.commands.TrustCommand(dataManager));

        getCommand("ghostblade")
                .setExecutor(new org.novagladecode.livesplugin.commands.GhostbladeCommand(this, dataManager));
        getCommand("dragonblade")
                .setExecutor(new org.novagladecode.livesplugin.commands.DragonbladeCommand(this, dataManager));
        getCommand("mistblade")
                .setExecutor(new org.novagladecode.livesplugin.commands.MistbladeCommand(this, dataManager));
        getCommand("soulblade")
                .setExecutor(new org.novagladecode.livesplugin.commands.SoulbladeCommand(this, dataManager));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new org.novagladecode.livesplugin.listeners.GameListener(this,
                dataManager, itemManager), this);
        getServer().getPluginManager().registerEvents(new org.novagladecode.livesplugin.listeners.MaceListener(this,
                wardenMaceCommand, netherMaceCommand, endMaceCommand), this);
        getServer().getPluginManager().registerEvents(new org.novagladecode.livesplugin.listeners.BladeListener(this),
                this);

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

    public boolean isAbilityEnabled(String bladeName) {
        return globalAbilityToggles.getOrDefault(bladeName.toLowerCase(), true);
    }

    public void setAbilityEnabled(String bladeName, boolean enabled) {
        globalAbilityToggles.put(bladeName.toLowerCase(), enabled);
    }

    public boolean isForgeActive() {
        return forgeActive;
    }

    public void setForgeActive(boolean active) {
        this.forgeActive = active;
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("Lives Plugin has been disabled!");
    }
}
