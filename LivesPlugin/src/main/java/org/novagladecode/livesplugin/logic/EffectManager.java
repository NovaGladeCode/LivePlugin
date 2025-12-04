package org.novagladecode.livesplugin.logic;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectManager {

    public void applyEffects(Player p, int level) {
        // Clear all effects first
        p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        p.removePotionEffect(PotionEffectType.SLOWNESS);
        p.removePotionEffect(PotionEffectType.HUNGER);
        p.removePotionEffect(PotionEffectType.GLOWING);
        p.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        p.removePotionEffect(PotionEffectType.LUCK);
        p.removePotionEffect(PotionEffectType.SPEED);
        // Do NOT remove invisibility here, as it's managed by command duration
        p.removePotionEffect(PotionEffectType.STRENGTH);

        // Apply health based on level
        applyHealth(p, level);

        // Debuffs - start with all at level 1, lose them as you level up
        if (level >= 1 && level < 2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 1 && level < 3) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 1 && level < 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 1 && level < 5) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
        }

        // Buffs - add progressively starting at level 6
        if (level >= 6) {
            p.addPotionEffect(
                    new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 7) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 8) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        }
        // Level 9 invisibility is now handled by /invis command

        if (level >= 10) {
            // Strength I only, no scaling
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false));
        }
    }

    private void applyHealth(Player p, int level) {
        // Level 10 and below: 10 hearts (20 HP - default)
        // Level 11-15: +1 heart per level above 10
        double maxHealth;
        if (level <= 10) {
            maxHealth = 20.0; // 10 hearts
        } else {
            // Level 11 = 22 HP (11 hearts), Level 15 = 30 HP (15 hearts)
            maxHealth = 20.0 + ((level - 10) * 2.0);
        }

        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);

        // Heal player to full health when health changes
        p.setHealth(maxHealth);
    }

    public void removeInvisibility(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    public void addInvisibility(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
    }
}
