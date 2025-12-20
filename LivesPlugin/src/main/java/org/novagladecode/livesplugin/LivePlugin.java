package org.novagladecode.livesplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.novagladecode.livesplugin.logic.ItemManager;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.commands.NetherMaceCommand;
import org.novagladecode.livesplugin.commands.WardenMaceCommand;
import org.novagladecode.livesplugin.commands.EndMaceCommand;
import org.novagladecode.livesplugin.commands.MaceCommand;
import org.novagladecode.livesplugin.commands.TrustCommand;
import org.novagladecode.livesplugin.commands.GhostbladeCommand;
import org.novagladecode.livesplugin.commands.DragonbladeCommand;
import org.novagladecode.livesplugin.commands.MistbladeCommand;
import org.novagladecode.livesplugin.commands.SoulbladeCommand;
import org.novagladecode.livesplugin.data.ForgeDataManager;
import org.novagladecode.livesplugin.logic.ForgeStructureManager;
import org.novagladecode.livesplugin.listeners.GameListener;
import org.novagladecode.livesplugin.listeners.MaceListener;
import org.novagladecode.livesplugin.listeners.BladeListener;
import org.novagladecode.livesplugin.gui.RecipeGUI;

public class LivePlugin extends JavaPlugin {

    private PlayerDataManager dataManager;
    private ItemManager itemManager;
    private WardenMaceCommand wardenMaceCommand;
    private NetherMaceCommand netherMaceCommand;
    private EndMaceCommand endMaceCommand;
    private HashMap<UUID, Boolean> maceInteractMode = new HashMap<>();
    private ForgeDataManager forgeDataManager;
    private ForgeStructureManager forgeStructureManager;
    private boolean forgeActive = false;
    private Map<String, Boolean> globalAbilityToggles = new HashMap<>();

    @Override
    public void onEnable() {
        // Initialize Managers
        this.dataManager = new PlayerDataManager(this);
        this.forgeDataManager = new ForgeDataManager(this);
        this.forgeStructureManager = new ForgeStructureManager(this);
        this.itemManager = new ItemManager(this);
        RecipeGUI recipeGUI = new RecipeGUI(itemManager);
        getServer().getPluginManager().registerEvents(recipeGUI, this);

        // Register Commands
        // Register Commands
        wardenMaceCommand = new WardenMaceCommand(this, dataManager);
        getCommand("wardenmace").setExecutor(wardenMaceCommand);

        netherMaceCommand = new NetherMaceCommand(this, dataManager);
        getCommand("nethermace").setExecutor(netherMaceCommand);

        endMaceCommand = new EndMaceCommand(this, dataManager);
        getCommand("endmace").setExecutor(endMaceCommand);

        getCommand("forge").setExecutor(new MaceCommand(this, dataManager));

        // getCommand("weapon").setExecutor(new
        // org.novagladecode.livesplugin.commands.WeaponCommand(itemManager, recipeGUI,
        // dataManager)); // Weapon command is now obsolete

        getCommand("trust").setExecutor(new TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new TrustCommand(dataManager));

        getCommand("ghostblade")
                .setExecutor(new GhostbladeCommand(this, dataManager));
        getCommand("dragonblade")
                .setExecutor(new DragonbladeCommand(this, dataManager));
        getCommand("mistblade")
                .setExecutor(new MistbladeCommand(this, dataManager));
        getCommand("soulblade")
                .setExecutor(new SoulbladeCommand(this, dataManager));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new GameListener(this,
                dataManager, itemManager), this);
        getServer().getPluginManager().registerEvents(new MaceListener(this,
                wardenMaceCommand, netherMaceCommand, endMaceCommand), this);
        getServer().getPluginManager().registerEvents(new BladeListener(this),
                this);

        // Register Recipes
        itemManager.init();

        getLogger().info("Lives Plugin has been enabled! (Lite Mode)");
    }

    public boolean isMaceInteractMode(UUID uuid) {
        return maceInteractMode.getOrDefault(uuid, false);
    }

    public void setMaceInteractMode(UUID uuid, boolean enabled) {
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

    public ForgeDataManager getForgeDataManager() {
        return forgeDataManager;
    }

    public ForgeStructureManager getForgeStructureManager() {
        return forgeStructureManager;
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        if (forgeDataManager != null) {
            forgeDataManager.saveData();
        }
        getLogger().info("Lives Plugin has been disabled!");
    }
}
