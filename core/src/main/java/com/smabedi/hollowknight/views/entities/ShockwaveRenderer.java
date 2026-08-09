package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.items.Shockwave;

/**
 * Handles the visual projection of the Shockwave physical entity.
 * Synchronizes the visual wave propagation with the underlying kinematic Box2D body.
 */
public class ShockwaveRenderer {
    private final Shockwave shockwave;
    private final Animation<TextureRegion> shockwaveAnim;
    private float stateTimer = 0f;

    public ShockwaveRenderer(Shockwave shockwave, TextureAtlas vfxAtlas) {
        this.shockwave = shockwave;
        // Normal playback ensures the animation cycles exactly once, reflecting the projectile's dissipation
        shockwaveAnim = new Animation<>(0.05f, vfxAtlas.findRegions("shockwave"), Animation.PlayMode.NORMAL);
    }

    public void render(Batch batch, float dt) {
        // Halt the rendering pipeline immediately if the underlying physical body has been destroyed
        if (shockwave.isDestroyed) return;

        stateTimer += dt;
        TextureRegion currentFrame = shockwaveAnim.getKeyFrame(stateTimer);

        // Align the visual orientation with the active horizontal velocity vector
        boolean movingRight = shockwave.b2body.getLinearVelocity().x > 0;

        if (movingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (!movingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        // Compute rendering bounds centered securely over the Box2D kinematic body
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;

        float x = shockwave.b2body.getPosition().x - (width / 2f);
        float y = shockwave.b2body.getPosition().y - (height / 2f);

        batch.draw(currentFrame, x, y, width, height);
    }

    /**
     * Exposes the animation completion status to external rendering orchestrators for memory cleanup.
     *
     * @return true if the visual sequence has concluded.
     */
    public boolean isAnimationFinished() {
        return shockwaveAnim.isAnimationFinished(stateTimer);
    }
}
