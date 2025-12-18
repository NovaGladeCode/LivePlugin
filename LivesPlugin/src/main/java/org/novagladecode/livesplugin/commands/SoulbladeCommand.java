package org.novagladecode.livesplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulbladeCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> cooldown1 = new HashMap<>();
    private final Map<UUID, Long> cooldown2 = new HashMap<>();

    public SoulbladeCommand(LivePlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!plugin.getItemManager().isSoulblade(item)) {
            p.sendMessage("§cYou must be holding the Soulblade!");
            return true;
        }

        if (!plugin.isAbilityEnabled("soulblade")) {
            p.sendMessage("§cSoulblade abilities are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /soulblade <1|2>");
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
            p.sendMessage("§cSoul Beam is on cooldown!");
            return;
        }

        // Soul Beam: High-impact spectral strike
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.5f);

        // Shoot particles in a line
        org.bukkit.util.Vector dir = p.getLocation().getDirection();
        for (double i = 1; i < 20; i += 0.5) {
            org.bukkit.Location loc = p.getEyeLocation().add(dir.clone().multiply(i));
            p.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, loc, 3, 0.1, 0.1, 0.1, 0.05);
            p.getWorld().spawnParticle(org.bukkit.Particle.SOUL, loc, 1, 0, 0, 0, 0);
        }

        // Damage logic in a raytrace
        org.bukkit.util.RayTraceResult result = p.getWorld().rayTraceEntities(p.getEyeLocation(),
                dir, 20, 1.0, (e) -> e != p && e instanceof org.bukkit.entity.LivingEntity);
        if (result != null && result.getHitEntity() != null) {
            org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) result.getHitEntity();
            target.damage(15.0, p);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1)); // Wither II for 5s
            target.sendMessage("§8§oYour soul has been scorched...");
            target.getWorld().spawnParticle(org.bukkit.Particle.SOUL, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5,
                    0.5, 0.1);
        }

        p.sendMessage("§8Soul Beam fired!");
        cooldown1.put(p.getUniqueId(), now + 15000); // 15s
    }

    public void useAbility2(Player p) {
        int points = dataManager.getPoints(p.getUniqueId());
        if (points < 10) {
            p.sendMessage("§cYou need Forge Level 10 to use this! (Current: " + points + "/10)");
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldown2.getOrDefault(p.getUniqueId(), 0L) > now) {
            p.sendMessage("§cDivine Protection is on cooldown!");
            return;
        }

        // Divine Protection: Invulnerability (10s)
        p.setInvulnerable(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
        p.sendMessage("§e§lDIVINE PROTECTION ACTIVATED!");
        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                p.setInvulnerable(false);
                p.setHealth(Math.min(p.getHealth(), 7.0)); // Down to 7 HP (3.5 hearts)
                p.sendMessage("§cDivine Protection has faded. You feel weak.");
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 0.5f);
            }
        }, 200L); // 10s

        cooldown2.put(p.getUniqueId(), now + 120000); // 2m
    }
}
