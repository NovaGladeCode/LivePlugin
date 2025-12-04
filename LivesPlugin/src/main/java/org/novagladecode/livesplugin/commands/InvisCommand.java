package org.novagladecode.livesplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.logic.EffectManager;

import java.util.UUID;

public class InvisCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final EffectManager effectManager;

    public InvisCommand(PlayerDataManager dataManager, EffectManager effectManager) {
        this.dataManager = dataManager;
        this.effectManager = effectManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        int level = dataManager.getLevel(uuid);
        if (level < 9) {
            p.sendMessage("§cYou need to be level 9 or higher to use this command!");
            return true;
        }

        boolean current = dataManager.isInvisibilityEnabled(uuid);
        dataManager.setInvisibilityEnabled(uuid, !current);

        if (!current) {
            effectManager.addInvisibility(p);
            p.sendMessage("§aInvisibility enabled!");
        } else {
            effectManager.removeInvisibility(p);
            p.sendMessage("§cInvisibility disabled!");
        }
        return true;
    }
}
