package com.smabedi.hollowknight.models.entities;

public interface IDamageable {
    void takeDamage(int amount);
    void applyKnockback(float forceX, float forceY);
    void die();
    boolean isDead();
}
