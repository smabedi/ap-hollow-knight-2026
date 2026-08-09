package com.smabedi.hollowknight.models.entities.items;

/**
 * Enumeration of all discrete visual effects triggered by game logic.
 * Acts as a decoupled bridge between the Model/Controller layers and the rendering View.
 */
public enum VfxType {
    DAMAGE,
    NORMAL_DASH,
    SHADOW_DASH,
    WRAITHS,
    VOID_WRAITHS,
    SPIRIT_CAST,
    VOID_SPIRIT_CAST
}
