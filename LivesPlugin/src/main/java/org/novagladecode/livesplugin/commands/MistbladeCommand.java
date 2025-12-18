package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MistbladeCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public MistbladeCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getItemManager().isMistblade(item)) {
            p.sendMessage("§cYou must be holding the Mistblade!");
            return true;
        }

        if (!plugin.isAbilityEnabled("mistblade")) {
            p.sendMessage("§cMistblade abilities are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /mistblade <1|2>");
            return true;
        }

        if (args[0].equals("1")) {
            useAbility1(p);
        } else if (args[0].equals("2")) {
            useAbility2(p);
        }

        return true;
    }

    public void useAbility1(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 5) {
            p.sendMessage("§cYou need Forge Level 5 to use this! (Current: " + points + "/5)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown1.getOrDefault(p.getUniqueId(), 0L) > now) {
            p.sendMessage("§cTrident Storm is on cooldown!");
            return;
        }

        // Trident Storm: Spawn spinning tridents
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            Vector offset = new Vector(Math.cos(angle) * 3, 3, Math.sin(angle) * 3);
            Trident t = p.getWorld().spawn(p.getLocation().add(offset), Trident.class);
            t.setShooter(p);
            t.setVelocity(new Vector(0, -1, 0));
        }

        p.sendMessage("§bTrident Storm!");
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 0.5f);
        cooldown1.put(p.getUniqueId(), now + 25000); // 25s
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need Forge Level 10 to use this! (Current: " + points + "/10)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown2.getOrDefault(p.getUniqueId(), 0L) > now) {
            p.sendMessage("§cMistburst is on cooldown!");
            return;
        }

        // Mistburst: "Make water rain on people" - I'll implement as a localized
        // weather effect/particles
        for (Entity e : p.getNearbyEntities(12, 12, 12)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                if (dataManager.isTrusted(p.getUniqueId(), target.getUniqueId()))
                    continue;
                target.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL);
                target.sendMessage("§9It's starting to pour... you feel cold.");

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (target.isOnline()) {
                        target.resetPlayerWeather();
                    }
                }, 200L); // 10s rain
            }
        }

        p.sendMessage("§bYou have summoned a localized mist.");
        p.playSound(p.getLocation(), Sound.WEATHER_RAIN, 1.0f, 1.0f);
        cooldown2.put(p.getUniqueId(), now + 60000); // 1m
    }
}
