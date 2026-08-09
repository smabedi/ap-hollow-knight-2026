package com.smabedi.hollowknight.models.entities.enemies;

/**
 * Enumerates the high-level tactical maneuvers available to the False Knight boss.
 * Selected dynamically by the underlying AI decision matrix based on spatial data and randomization.
 */
public enum BossMove {
    IDLE,
    MACE_SLAM,
    CHARGE,
    OFFENSIVE_LEAP,
    DEFENSIVE_LEAP,
    POWER_SLAM
}
