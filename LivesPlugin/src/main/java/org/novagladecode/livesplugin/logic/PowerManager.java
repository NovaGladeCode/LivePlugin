package org.novagladecode.livesplugin.logic;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.novagladecode.livesplugin.data.PlayerDataManager;

public class PowerManager implements Listener {

    private final PlayerDataManager dataManager;

    public PowerManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player p = (Player) e.getEntity();
        String powerName = dataManager.getPower(p.getUniqueId());
        PowerType power = PowerType.valueOf(powerName);

        if (power == PowerType.NONE)
            return;

        // Handle projectile replacement powers
        switch (power) {
            case NETHER:
                e.setCancelled(true);
                p.launchProjectile(Fireball.class);
                break;
            case END:
                e.setCancelled(true);
                p.launchProjectile(DragonFireball.class);
                break;
            case WITHER:
                e.setCancelled(true);
                p.launchProjectile(WitherSkull.class);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player))
            return;

        Player shooter = (Player) e.getEntity().getShooter();
        String powerName = dataManager.getPower(shooter.getUniqueId());
        PowerType power = PowerType.valueOf(powerName);

        if (power == PowerType.NONE)
            return;

        // Handle on-hit effect powers
        if (e.getEntity() instanceof Arrow) {
            Entity hitEntity = e.getHitEntity();
            Location hitLoc = e.getEntity().getLocation();

            switch (power) {
                case SKY:
                    hitLoc.getWorld().strikeLightning(hitLoc);
                    if (hitEntity instanceof LivingEntity) {
                        ((LivingEntity) hitEntity).addPotionEffect(
                                new PotionEffect(PotionEffectType.LEVITATION, 60, 1)); // 3 seconds
                    }
                    break;
                case OCEAN:
                    if (hitEntity instanceof LivingEntity) {
                        ((LivingEntity) hitEntity).addPotionEffect(
                                new PotionEffect(PotionEffectType.POISON, 100, 1)); // 5 seconds
                    }
                    break;
                case OVERWORLD:
                    if (hitEntity instanceof LivingEntity) {
                        ((LivingEntity) hitEntity).addPotionEffect(
                                new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0));
                        ((LivingEntity) hitEntity).addPotionEffect(
                                new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); // 3 seconds
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
