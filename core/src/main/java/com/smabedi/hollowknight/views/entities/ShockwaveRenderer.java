package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.items.Shockwave;

public class ShockwaveRenderer {
    private final Shockwave shockwave;
    private float stateTimer = 0f;
    private final Animation<TextureRegion> shockwaveAnim;

    public ShockwaveRenderer(Shockwave shockwave, TextureAtlas vfxAtlas) {
        this.shockwave = shockwave;
        // PlayMode.NORMAL ensures it plays exactly once and stops (or holds the last dissipated frame)
        // Adjust the frame duration (e.g., 0.05f) to perfectly match the shockwave's lifespan
        shockwaveAnim = new Animation<>(0.05f, vfxAtlas.findRegions("shockwave"), Animation.PlayMode.NORMAL);
    }

    public void render(Batch batch, float dt) {
        // If the shockwave is logically destroyed, stop rendering
        if (shockwave.isDestroyed) return;

        stateTimer += dt;
        TextureRegion currentFrame = shockwaveAnim.getKeyFrame(stateTimer);

        // Flipping logic (Asset faces RIGHT by default)
        boolean movingRight = shockwave.b2body.getLinearVelocity().x > 0;

        if (movingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Needs to face right, so UN-FLIP
        } else if (!movingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Needs to face left, so FLIP
        }

        // Draw
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;

        // Center the sprite on the Box2D body
        float x = shockwave.b2body.getPosition().x - (width / 2f);
        float y = shockwave.b2body.getPosition().y - (height / 2f);

        batch.draw(currentFrame, x, y, width, height);
    }

    // Optional: Let the GameScreen know when the animation is completely finished
    // so it can clean up the renderer from the array
    public boolean isAnimationFinished() {
        return shockwaveAnim.isAnimationFinished(stateTimer);
    }
}
