package org.novagladecode.livesplugin.commands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DragonBreathCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        // Check for Dragon Egg in inventory
        if (!p.getInventory().contains(Material.DRAGON_EGG)) {
            p.sendMessage("§cYou need a Dragon Egg in your inventory to use this command!");
            return true;
        }

        // Spawn Dragon Breath at player's location
        AreaEffectCloud cloud = (AreaEffectCloud) p.getWorld().spawnEntity(p.getLocation(),
                EntityType.AREA_EFFECT_CLOUD);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setRadius(3.0f);
        cloud.setDuration(200); // 10 seconds
        cloud.setSource(p); // Set source so we know who spawned it (optional, but good practice)

        // Add Instant Damage II (Stronger than default)
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);

        p.sendMessage("§5You unleashed the Dragon's Breath!");

        return true;
    }
}
