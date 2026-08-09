package com.smabedi.hollowknight.controllers;

import com.smabedi.hollowknight.models.entities.enemies.EnemyType;
import com.smabedi.hollowknight.models.entities.items.VfxType;

/**
 * Decouples the physics models and controllers from the view layer.
 * Defines a contract for broadcasting visual and state-based events.
 */
public interface EventCallback {
    void spawnStaticVfx(VfxType type, float x, float y, float offsetX, float offsetY, boolean facingRight, boolean defaultFacesRight);

    void addCameraTrauma(float amount);

    void setCameraTrauma(float amount);

    void onBossDeath();

    void onPlayerDeath();

    void onEnemyDeath(EnemyType enemyType);
}
