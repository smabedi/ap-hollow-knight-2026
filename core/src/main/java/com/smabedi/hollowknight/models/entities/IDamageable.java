package com.smabedi.hollowknight.models.entities;

/**
 * Contract for any entity in the game world that can interact with the combat system.
 * Ensures consistent handling of health modification, momentum shifts, and death states.
 */
public interface IDamageable {
    void takeDamage(int amount);

    void applyKnockback(float forceX, float forceY);

    void die();

    boolean isDead();
}
