package com.smabedi.hollowknight.models.entities.enemies;

/**
 * Enumerates the behavioral states for the Crystal Guardian AI model.
 * Orchestrates the sequence from target acquisition to laser discharge and repositioning.
 */
public enum GuardianState {
    IDLE,
    PREPPING_LASER,
    FIRING_LASER,
    ENRAGED,
    RETURNING
}
