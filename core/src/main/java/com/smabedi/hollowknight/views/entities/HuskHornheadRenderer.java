package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.enemies.HornheadState;
import com.smabedi.hollowknight.models.entities.enemies.HuskHornhead;

public class HuskHornheadRenderer implements EntityRenderer {
    private final HuskHornhead hornhead;
    private float stateTimer = 0f;
    private boolean facingRight = true;

    // Visual states diverge slightly from logical states to allow for transition animations
    private enum VisualState { IDLE, WALK, ANTICIPATE, ATTACK, DEAD }
    private VisualState currentVisualState = VisualState.WALK;

    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> anticipateAnim;
    private final Animation<TextureRegion> attackAnim;
    private final Animation<TextureRegion> deathAnim;

    public HuskHornheadRenderer(HuskHornhead hornhead, TextureAtlas atlas) {
        this.hornhead = hornhead;
        idleAnim = new Animation<>(0.1f, atlas.findRegions("hornhead_idle"), Animation.PlayMode.LOOP);
        walkAnim = new Animation<>(0.1f, atlas.findRegions("hornhead_walk"), Animation.PlayMode.LOOP);
        // Normal play mode so it doesn't loop while we wait for it to finish
        anticipateAnim = new Animation<>(0.05f, atlas.findRegions("hornhead_anticipate"), Animation.PlayMode.NORMAL);
        attackAnim = new Animation<>(0.08f, atlas.findRegions("hornhead_attack"), Animation.PlayMode.LOOP);
        deathAnim = new Animation<>(0.1f, atlas.findRegions("hornhead_death"), Animation.PlayMode.LOOP);
    }

    @Override
    public void render(Batch batch, float dt) {
        VisualState nextState = currentVisualState;
        HornheadState logicState = hornhead.getCurrentState();

        // 1. Map Logical State to Visual State
        if (hornhead.isDead()) {
            nextState = VisualState.DEAD;
        } else if (logicState == HornheadState.RESTING) {
            nextState = VisualState.IDLE;
        } else if (logicState == HornheadState.WALKING) {
            nextState = VisualState.WALK;
        } else if (logicState == HornheadState.CHARGING) {
            // INTERCEPT: If we just started charging, play anticipate first!
            if (currentVisualState != VisualState.ANTICIPATE && currentVisualState != VisualState.ATTACK) {
                nextState = VisualState.ANTICIPATE;
            }
            // Once anticipate is fully finished, proceed to the full attack loop
            else if (currentVisualState == VisualState.ANTICIPATE && anticipateAnim.isAnimationFinished(stateTimer)) {
                nextState = VisualState.ATTACK;
            }
        }

        // 2. Reset timer on state transitions
        if (nextState != currentVisualState) {
            stateTimer = 0f;
            currentVisualState = nextState;
        }

        stateTimer += dt;
        TextureRegion currentFrame;

        switch (currentVisualState) {
            case DEAD -> currentFrame = deathAnim.getKeyFrame(stateTimer);
            case IDLE -> currentFrame = idleAnim.getKeyFrame(stateTimer);
            case ANTICIPATE -> currentFrame = anticipateAnim.getKeyFrame(stateTimer);
            case ATTACK -> currentFrame = attackAnim.getKeyFrame(stateTimer);
            default -> currentFrame = walkAnim.getKeyFrame(stateTimer);
        }

        // 3. Flipping logic (Default asset faces LEFT)
        float velX = hornhead.b2body.getLinearVelocity().x;

        if (velX > 0.2f) {
            facingRight = true;
        } else if (velX < -0.2f) {
            facingRight = false;
        }

        if (facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (!facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        // 4. Draw
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = hornhead.b2body.getPosition().x - (width / 2f);
        float y = hornhead.b2body.getPosition().y - (height / 2f) + Constants.Enemy.HuskHornhead.HEIGHT_HALVED_SCALED * 0.5f;

        batch.draw(currentFrame, x, y, width, height);
    }
}
