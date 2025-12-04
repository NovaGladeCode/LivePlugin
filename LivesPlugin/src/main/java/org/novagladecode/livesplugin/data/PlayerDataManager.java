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
    private final Map<UUID, Boolean> invisibilityToggle = new HashMap<>();

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
            dataConfig.set(uuid + ".lives", 5);
            dataConfig.set(uuid + ".level", 1);
            dataConfig.set(uuid + ".banned", false);
            saveData();
        }
    }

    public int getLives(UUID uuid) {
        return dataConfig.getInt(uuid + ".lives");
    }

    public void setLives(UUID uuid, int lives) {
        dataConfig.set(uuid + ".lives", lives);
    }

    public int getLevel(UUID uuid) {
        return dataConfig.getInt(uuid + ".level");
    }

    public void setLevel(UUID uuid, int level) {
        dataConfig.set(uuid + ".level", level);
    }

    public boolean isBanned(UUID uuid) {
        return dataConfig.getBoolean(uuid + ".banned");
    }

    public void setBanned(UUID uuid, boolean banned) {
        dataConfig.set(uuid + ".banned", banned);
    }

    public boolean isInvisibilityEnabled(UUID uuid) {
        return invisibilityToggle.getOrDefault(uuid, true);
    }

    public void setInvisibilityEnabled(UUID uuid, boolean enabled) {
        invisibilityToggle.put(uuid, enabled);
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
}
