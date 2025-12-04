package org.novagladecode.livesplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.commands.InvisCommand;
import org.novagladecode.livesplugin.commands.LevelCommand;
import org.novagladecode.livesplugin.commands.LiveCommand;
import org.novagladecode.livesplugin.commands.UnbanCommand;
import org.novagladecode.livesplugin.data.PlayerDataManager;
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

        // Register Recipes
        itemManager.registerUnbanRecipe();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new GameListener(this, dataManager, itemManager, effectManager),
                this);

        // Register Commands
        getCommand("level").setExecutor(new LevelCommand(dataManager));
        getCommand("invis").setExecutor(new InvisCommand(dataManager, effectManager));
        getCommand("live").setExecutor(new LiveCommand(dataManager));
        getCommand("unban").setExecutor(new UnbanCommand(dataManager, itemManager));

        getLogger().info("Lives Plugin has been enabled!");

        // Apply effects to all online players
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int level = dataManager.getLevel(p.getUniqueId());
                boolean invis = dataManager.isInvisibilityEnabled(p.getUniqueId());
                effectManager.applyEffects(p, level, invis);
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
