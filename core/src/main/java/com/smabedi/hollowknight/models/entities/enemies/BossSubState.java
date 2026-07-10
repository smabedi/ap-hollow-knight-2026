package com.smabedi.hollowknight.models.entities.enemies;

public enum BossSubState {
    WIND_UP,     // Anticipate / Preparing to jump
    ACTIVE,      // Midair or charging forward
    ATTACK,      // The exact moment of smashing the ground
    RECOVERY,    // Standing back up / recovering from smash
    NONE,        // Idle
    STUN_HIT     // When hit while armor is open
}
