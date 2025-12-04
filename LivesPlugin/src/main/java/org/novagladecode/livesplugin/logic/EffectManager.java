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

        // Apply effects based on level (good effects stack, bad ones don't)
        if (level >= 1) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 3) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 0, true, false));
        }
        if (level >= 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
        }
        // Level 5 = nothing

        // Good effects that stack
        int goodLevels = Math.max(0, level - 5);

        if (level >= 6) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE,
                    Math.min(goodLevels - 1, 4), true, false));
        }
        if (level >= 7) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE,
                    Math.min(goodLevels - 1, 4) + 1, true, false));
        }
        if (level >= 8) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Math.min(goodLevels - 1, 4),
                    true, false));
        }
        if (level >= 9) {
            if (invisEnabled) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
            }
        }
        if (level >= 10) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE,
                    Math.min(goodLevels - 1, 4), true, false));
        }
    }

    public void removeInvisibility(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    public void addInvisibility(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
    }
}
