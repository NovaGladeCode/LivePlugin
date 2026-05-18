package org.novagladecode.livesplugin.warlord;

public final class WarlordConfig {
    private WarlordConfig() {}

    // --- Boss Stats ---
    public static final double BOSS_MAX_HEALTH       = 500.0;
    public static final double BOSS_SPEED_P1         = 0.28;
    public static final double BOSS_SPEED_P3         = 0.38;
    public static final double BOSS_ATTACK_P1        = 8.0;
    public static final double BOSS_ATTACK_P3        = 14.0;
    public static final double BOSS_ARMOR            = 10.0;
    public static final double BOSS_SCALE            = 1.0; // Wardens already large

    // --- Phase Transition ---
    public static final double PHASE2_HEALTH_THRESHOLD = 0.50; // 50% HP triggers phase 2

    // --- Spawner Stats ---
    public static final double SPAWNER_MAX_HEALTH    = 60.0;
    public static final int    SPAWNER_COUNT_BASE    = 3;   // +1 per 2 extra players
    public static final int    SPAWN_INTERVAL_TICKS  = 120; // 6 seconds
    public static final int    SPAWNER_RADIUS        = 22;  // blocks from boss on phase2 start
    public static final int    MAX_MINIONS_PER_SPAWNER = 5;

    // --- Attack Cooldowns (ticks) ---
    public static final int CD_GROUND_SLAM   = 80;
    public static final int CD_DASH          = 100;
    public static final int CD_SUMMON        = 160;
    public static final int CD_SHOCKWAVE     = 120;
    public static final int CD_FIREBALL      = 90;
    public static final int CD_PULL          = 140;

    // --- Rage Mode ---
    public static final double RAGE_HEALTH_THRESHOLD = 0.25; // 25% HP
    public static final int    RAGE_DURATION_TICKS   = 200;  // 10 seconds

    // --- Compass Update ---
    public static final int COMPASS_UPDATE_TICKS = 10; // every 0.5s
}
