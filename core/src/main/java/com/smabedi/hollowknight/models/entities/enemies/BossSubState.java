package com.smabedi.hollowknight.models.entities.enemies;

/**
 * Represents the granular microstates within a boss entity's active maneuver.
 * Used to synchronize rendering frames with precise physical hitbox activations and recovery periods.
 */
public enum BossSubState {
    WIND_UP,
    ACTIVE,
    ATTACK,
    RECOVERY,
    NONE,
    STUN_HIT
}
