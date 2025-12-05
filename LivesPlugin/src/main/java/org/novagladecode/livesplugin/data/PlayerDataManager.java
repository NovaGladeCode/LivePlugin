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
            dataConfig.set(uuid + ".lives", 5);
            dataConfig.set(uuid + ".level", 5);
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
        if (level > 15)
            level = 15;
        dataConfig.set(uuid + ".level", level);
    }

    public boolean isBanned(UUID uuid) {
        return dataConfig.getBoolean(uuid + ".banned");
    }

    public void setBanned(UUID uuid, boolean banned) {
        dataConfig.set(uuid + ".banned", banned);
    }

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

    public java.util.List<UUID> getBannedPlayers() {
        java.util.List<UUID> bannedPlayers = new java.util.ArrayList<>();
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                if (dataConfig.getBoolean(uuid + ".banned")) {
                    bannedPlayers.add(uuid);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return bannedPlayers;
    }

    public boolean isWardenMaceCrafted() {
        return dataConfig.getBoolean("global.wardenMaceCrafted", false);
    }

    public void setWardenMaceCrafted(boolean crafted) {
        dataConfig.set("global.wardenMaceCrafted", crafted);
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
