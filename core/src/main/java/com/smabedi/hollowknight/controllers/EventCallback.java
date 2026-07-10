package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface EventCallback {
    void spawnStaticVfx(Animation<TextureRegion> anim, float x, float y, float offsetX, float offsetY, boolean facingRight, boolean defaultFacesRight);
    void addCameraTrauma(float amount);
    void setCameraTrauma(float amount);
    void onBossDeath();
    void onPlayerDeath();
    void onEnemyDeath();
}
