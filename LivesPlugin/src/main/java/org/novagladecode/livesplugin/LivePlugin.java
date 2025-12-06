package org.novagladecode.livesplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.commands.InvisCommand;
import org.novagladecode.livesplugin.commands.LiveCommand;
import org.novagladecode.livesplugin.commands.AirBoostCommand;
import org.novagladecode.livesplugin.commands.DragonBreathCommand;
import org.novagladecode.livesplugin.commands.MaceCommand;
import org.novagladecode.livesplugin.commands.NetherMaceCommand;
import org.novagladecode.livesplugin.commands.WardenMaceCommand;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.gui.UnbanGUI;
import org.novagladecode.livesplugin.listeners.GameListener;
import org.novagladecode.livesplugin.logic.EffectManager;
import org.novagladecode.livesplugin.logic.ItemManager;

public class LivePlugin extends JavaPlugin {

    private PlayerDataManager dataManager;
    private EffectManager effectManager;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        // Initialize Managers
        this.dataManager = new PlayerDataManager(this);
        this.effectManager = new EffectManager();
        this.itemManager = new ItemManager(this);
        UnbanGUI unbanGUI = new UnbanGUI(dataManager, itemManager);

        // Register Recipes
        itemManager.registerUnbanRecipe();
        itemManager.registerWardenMaceRecipe();
        itemManager.registerNetherMaceRecipe();
        itemManager.registerChickenBowRecipe();

        // Register Listeners
        getServer().getPluginManager().registerEvents(
                new GameListener(this, dataManager, itemManager, effectManager, unbanGUI),
                this);
        getServer().getPluginManager().registerEvents(unbanGUI, this);

        // Register Commands
        // Register Commands
        getCommand("invis").setExecutor(new InvisCommand(this, dataManager, effectManager));
        getCommand("life").setExecutor(new LiveCommand(dataManager, itemManager));
        getCommand("airboost").setExecutor(new AirBoostCommand(dataManager));
        getCommand("dragonbreath").setExecutor(new DragonBreathCommand());
        getCommand("mace").setExecutor(new MaceCommand());
        getCommand("nethermace").setExecutor(new NetherMaceCommand(this, dataManager));
        getCommand("wardenmace").setExecutor(new WardenMaceCommand(this, dataManager));
        getCommand("trust").setExecutor(new org.novagladecode.livesplugin.commands.TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new org.novagladecode.livesplugin.commands.TrustCommand(dataManager));

        getLogger().info("Lives Plugin has been enabled!");

        // Apply effects to all online players
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int level = dataManager.getLevel(p.getUniqueId());
                effectManager.applyEffects(p, level);
            }
        }, 0L, 100L);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("Lives Plugin has been disabled!");
    }
}
