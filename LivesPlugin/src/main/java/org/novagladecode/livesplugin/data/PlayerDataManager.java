package org.novagladecode.livesplugin.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Long> invisCooldowns = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            plugin.saveResource("playerdata.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializePlayer(UUID uuid) {
        if (!dataConfig.contains(uuid.toString())) {
            dataConfig.set(uuid + ".points", 2);
            saveData();
        }
    }

    public int getPoints(UUID uuid) {
        return dataConfig.getInt(uuid + ".points", 0);
    }

    public void setPoints(UUID uuid, int points) {
        dataConfig.set(uuid + ".points", points);
        saveData();
    }

    public void addPoint(UUID uuid) {
        int current = getPoints(uuid);
        if (current < 10) { // Max Forge Level is 10
            setPoints(uuid, current + 1);
        }
    }

    public void removePoint(UUID uuid) {
        int current = getPoints(uuid);
        if (current > 0) {
            setPoints(uuid, current - 1);
        }
    }

    public void resetPoints(UUID uuid) {
        setPoints(uuid, 0);
    }

    // Removing Lives/Levels/Ban logic as requested

    public long getInvisCooldown(UUID uuid) {
        return invisCooldowns.getOrDefault(uuid, 0L);
    }

    public void setInvisCooldown(UUID uuid, long timestamp) {
        invisCooldowns.put(uuid, timestamp);
    }

    public UUID getPlayerUUIDByName(String name) {
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID checkUUID = UUID.fromString(key);
                if (Bukkit.getOfflinePlayer(checkUUID).getName().equalsIgnoreCase(name)) {
                    return checkUUID;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public boolean isWardenMaceCrafted() {
        return dataConfig.getBoolean("global.wardenMaceCrafted", false);
    }

    public void setWardenMaceCrafted(boolean crafted) {
        dataConfig.set("global.wardenMaceCrafted", crafted);
        saveData();
    }

    public boolean isNetherMaceCrafted() {
        return dataConfig.getBoolean("global.netherMaceCrafted", false);
    }

    public void setNetherMaceCrafted(boolean crafted) {
        dataConfig.set("global.netherMaceCrafted", crafted);
        saveData();
    }

    public boolean isEndMaceCrafted() {
        return dataConfig.getBoolean("global.endMaceCrafted", false);
    }

    public void setEndMaceCrafted(boolean crafted) {
        dataConfig.set("global.endMaceCrafted", crafted);
        saveData();
    }

    public void addTrusted(UUID owner, UUID target) {
        java.util.List<String> trusted = dataConfig.getStringList(owner + ".trusted");
        if (!trusted.contains(target.toString())) {
            trusted.add(target.toString());
            dataConfig.set(owner + ".trusted", trusted);
            saveData();
        }
    }

    public void removeTrusted(UUID owner, UUID target) {
        java.util.List<String> trusted = dataConfig.getStringList(owner + ".trusted");
        if (trusted.contains(target.toString())) {
            trusted.remove(target.toString());
            dataConfig.set(owner + ".trusted", trusted);
            saveData();
        }
    }

    public boolean isTrusted(UUID owner, UUID target) {
        if (owner.equals(target))
            return true; // Always trust self
        java.util.List<String> trusted = dataConfig.getStringList(owner + ".trusted");
        return trusted.contains(target.toString());
    }
}
