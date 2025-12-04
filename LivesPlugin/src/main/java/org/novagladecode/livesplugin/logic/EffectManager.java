package org.novagladecode.livesplugin.logic;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectManager {

    public void applyEffects(Player p, int level, boolean invisEnabled) {
        // Clear all effects first
        p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        p.removePotionEffect(PotionEffectType.SLOWNESS);
        p.removePotionEffect(PotionEffectType.HUNGER);
        p.removePotionEffect(PotionEffectType.GLOWING);
        p.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        p.removePotionEffect(PotionEffectType.LUCK);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.removePotionEffect(PotionEffectType.STRENGTH);

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
        if (level >= 9) {
            if (invisEnabled) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
            }
        }
        if (level >= 10) {
            // Strength I only, no scaling
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false));
        }
    }

    public void removeInvisibility(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    public void addInvisibility(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
    }
}
