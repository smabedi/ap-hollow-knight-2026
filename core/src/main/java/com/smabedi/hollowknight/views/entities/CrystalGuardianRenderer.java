package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.enemies.CrystalGuardian;
import com.smabedi.hollowknight.models.entities.enemies.GuardianState;

public class CrystalGuardianRenderer implements EntityRenderer {
    private final CrystalGuardian guardian;
    private float stateTimer = 0f;

    private enum VisualState { IDLE, SHOOT, RUN, DEAD }
    private VisualState currentVisualState = VisualState.IDLE;

    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> runAnim;
    private final Animation<TextureRegion> shootAnim;
    private final Animation<TextureRegion> deathAnim;

    // Laser VFX
    private final Animation<TextureRegion> laserOriginAnim;
    private final Animation<TextureRegion> laserBodyAnim;
    private final TextureRegion laserGlowFrame;

    public CrystalGuardianRenderer(CrystalGuardian guardian, TextureAtlas atlas) {
        this.guardian = guardian;
        idleAnim = new Animation<>(0.2f, atlas.findRegions("crystallized_idle"), Animation.PlayMode.LOOP);
        runAnim = new Animation<>(0.1f, atlas.findRegions("crystallized_run"), Animation.PlayMode.LOOP);
        shootAnim = new Animation<>(0.2f, atlas.findRegions("crystallized_shoot"), Animation.PlayMode.LOOP);
        deathAnim = new Animation<>(0.1f, atlas.findRegions("crystallized_death"), Animation.PlayMode.NORMAL);

        laserOriginAnim = new Animation<>(0.1f, atlas.findRegions("crystal_laser_origin"), Animation.PlayMode.NORMAL);
        laserBodyAnim = new Animation<>(0.075f, atlas.findRegions("crystal_laser"), Animation.PlayMode.LOOP);
        laserGlowFrame = atlas.findRegion("crystal_laser_glow");
    }

    @Override
    public void render(Batch batch, float dt) {
        VisualState nextState = currentVisualState;
        GuardianState logicState = guardian.getCurrentState();

        // 1. Map Logical State to Visual State smoothly
        if (guardian.isDead()) {
            nextState = VisualState.DEAD;
        } else if (logicState == GuardianState.IDLE || logicState == GuardianState.PREPPING_LASER) {
            nextState = VisualState.IDLE;
        } else if (logicState == GuardianState.FIRING_LASER) {
            nextState = VisualState.SHOOT; // Plays the shoot animation
        } else {
            nextState = VisualState.RUN; // Enraged or Returning
        }

        // 2. State Transition (Reset animation timer on change)
        if (nextState != currentVisualState) {
            stateTimer = 0f;
            currentVisualState = nextState;
        }

        stateTimer += dt; // Must count UP for LibGDX Animations
        TextureRegion currentFrame;

        switch (currentVisualState) {
            case DEAD -> currentFrame = deathAnim.getKeyFrame(stateTimer);
            case SHOOT -> currentFrame = shootAnim.getKeyFrame(stateTimer);
            case RUN -> currentFrame = runAnim.getKeyFrame(stateTimer);
            default -> currentFrame = idleAnim.getKeyFrame(stateTimer);
        }

        // 3. Flipping logic (Assets face LEFT by default)
        boolean facingRight = guardian.isFacingRight();
        if (facingRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        else if (!facingRight && currentFrame.isFlipX()) currentFrame.flip(true, false);

        // 4. Draw Guardian Body
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float centerX = guardian.b2body.getPosition().x;
        float centerY = guardian.b2body.getPosition().y;

        batch.draw(currentFrame, centerX - (width / 2f), centerY - (height / 2f), width, height);

        // 5. Draw Laser VFX (Only when Model is explicitly in FIRING_LASER!)
        if (logicState == GuardianState.FIRING_LASER && !guardian.isDead()) {
            float direction = facingRight ? 1f : -1f;
            float stomachOffsetX = 0.4f * direction;
            float startX = centerX + stomachOffsetX;
            float startY = centerY - 0.1f;

            float laserMaxRange = Constants.Enemy.CrystalGuardian.VISION_RANGE;
            float drawX = facingRight ? startX : startX - laserMaxRange;

            // 5a. Draw Glow
            float glowHeight = laserGlowFrame.getRegionHeight() / Constants.World.PPM;
            batch.draw(laserGlowFrame, drawX, startY - (glowHeight / 2f), laserMaxRange, glowHeight);

            // 5b. Draw Animated Laser Body
            TextureRegion currentLaserFrame = laserBodyAnim.getKeyFrame(stateTimer);
            float laserHeight = currentLaserFrame.getRegionHeight() / Constants.World.PPM;

            if (facingRight && !currentLaserFrame.isFlipX()) currentLaserFrame.flip(true, false);
            else if (!facingRight && currentLaserFrame.isFlipX()) currentLaserFrame.flip(true, false);

            batch.draw(currentLaserFrame, drawX, startY - (laserHeight / 2f), laserMaxRange, laserHeight);

            // 5c. Draw Origin Animation
            if (!laserOriginAnim.isAnimationFinished(stateTimer)) {
                TextureRegion originFrame = laserOriginAnim.getKeyFrame(stateTimer);

                if (facingRight && !originFrame.isFlipX()) originFrame.flip(true, false);
                else if (!facingRight && originFrame.isFlipX()) originFrame.flip(true, false);

                float oW = originFrame.getRegionWidth() / Constants.World.PPM;
                float oH = originFrame.getRegionHeight() / Constants.World.PPM;
                batch.draw(originFrame, startX - (oW / 2f) - 0.8f, startY - (oH / 2f), oW, oH);
            }
        }
    }
}
