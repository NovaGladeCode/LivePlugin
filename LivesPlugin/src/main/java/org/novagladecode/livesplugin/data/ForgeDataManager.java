package org.novagladecode.livesplugin.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ForgeDataManager {

    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final List<Location> forgeLocations = new ArrayList<>();

    public ForgeDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "forgedata.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        List<String> locStrings = dataConfig.getStringList("forges");
        for (String s : locStrings) {
            Location loc = stringToLoc(s);
            if (loc != null) {
                forgeLocations.add(loc);
            }
        }
    }

    public void saveData() {
        List<String> locStrings = new ArrayList<>();
        for (Location loc : forgeLocations) {
            locStrings.add(locToString(loc));
        }
        dataConfig.set("forges", locStrings);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addForge(Location loc) {
        if (!forgeLocations.contains(loc)) {
            forgeLocations.add(loc);
            saveData();
        }
    }

    public void removeForge(Location loc) {
        if (forgeLocations.remove(loc)) {
            saveData();
        }
    }

    public List<Location> getForgeLocations() {
        return new ArrayList<>(forgeLocations);
    }

    public Location getRandomForge() {
        if (forgeLocations.isEmpty())
            return null;
        return forgeLocations.get(new Random().nextInt(forgeLocations.size()));
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLoc(String s) {
        String[] parts = s.split(",");
        if (parts.length < 4)
            return null;
        try {
            return new Location(Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
        } catch (Exception e) {
            return null;
        }
    }
}
