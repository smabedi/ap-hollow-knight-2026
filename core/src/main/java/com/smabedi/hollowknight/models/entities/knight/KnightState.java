package com.smabedi.hollowknight.models.entities.knight;

/**
 * Defines all possible discrete operational and visual states for the Knight.
 * Evaluated by rendering systems to match animations accurately with physical mechanics.
 */
public enum KnightState {
    IDLE,
    WALKING,
    JUMPING,
    DOUBLE_JUMPING,
    FALLING,
    DASHING,
    SHADOW_DASHING,
    WALL_SLIDING,
    ATTACKING_SIDE,
    ATTACKING_DOWN,
    ATTACKING_UP,
    FOCUSING,
    CASTING_WRAITHS,
    CASTING_VOID_WRAITHS,
    CASTING_SPIRIT,
    CASTING_VOID_SPIRIT
}
