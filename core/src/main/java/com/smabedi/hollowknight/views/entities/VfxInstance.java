package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Data transfer object functioning as a decoupled visual tracker.
 * Maps transient particle effects and spell animations to specific
 * coordinates without relying on persistent Box2D physics bodies.
 */
public class VfxInstance {
    public Animation<TextureRegion> animation;
    public float timer = 0f;
    public float x, y, offsetX, offsetY;
    public boolean facingRight, defaultFacesRight;

    public VfxInstance(Animation<TextureRegion> animation, float x, float y, float offsetX, float offsetY, boolean facingRight, boolean defaultFacesRight) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.facingRight = facingRight;
        this.defaultFacesRight = defaultFacesRight;
    }
}
