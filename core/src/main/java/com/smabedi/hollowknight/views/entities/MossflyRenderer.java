package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.enemies.Mossfly;

/**
 * Manages the rendering lifecycle of the Mossfly entity.
 * Translates the dynamic stealth and pursuit states into a cohesive visual sequence.
 */
public class MossflyRenderer implements EntityRenderer {
    private final Mossfly mossfly;
    private final Animation<TextureRegion> shakeAnim;
    private final Animation<TextureRegion> appearAnim;
    private final Animation<TextureRegion> flyAnim;
    private final Animation<TextureRegion> deathAnim;
    private float stateTimer = 0f;
    private State currentState = State.HIDDEN;

    public MossflyRenderer(Mossfly mossfly, TextureAtlas atlas) {
        this.mossfly = mossfly;
        shakeAnim = new Animation<>(0.2f, atlas.findRegions("mossfly_shake"), Animation.PlayMode.LOOP);
        appearAnim = new Animation<>(0.05f, atlas.findRegions("mossfly_appear"), Animation.PlayMode.NORMAL);
        flyAnim = new Animation<>(0.1f, atlas.findRegions("mossfly_fly"), Animation.PlayMode.LOOP);
        deathAnim = new Animation<>(0.1f, atlas.findRegions("mossfly_death"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void render(Batch batch, float dt) {
        State nextState = currentState;

        // Project the underlying logical AI state onto the visual rendering enumerator
        if (mossfly.isDead()) {
            nextState = State.DEAD;
        } else if (mossfly.isHidden()) {
            nextState = State.HIDDEN;
        } else if (currentState == State.HIDDEN) {
            // Intercept the transition from hidden to flying to inject the appearance animation
            nextState = State.APPEARING;
        } else if (currentState == State.APPEARING && appearAnim.isAnimationFinished(stateTimer)) {
            // Await completion of the appearance sequence before defaulting to the continuous flight loop
            nextState = State.FLYING;
        }

        // Enforce state transition resets to guarantee animations begin at the correct initial frame
        if (nextState != currentState) {
            stateTimer = 0f;
            currentState = nextState;
        }

        stateTimer += dt;
        TextureRegion currentFrame;

        switch (currentState) {
            case DEAD -> currentFrame = deathAnim.getKeyFrame(stateTimer);
            case APPEARING -> currentFrame = appearAnim.getKeyFrame(stateTimer);
            case FLYING -> currentFrame = flyAnim.getKeyFrame(stateTimer);
            default -> currentFrame = shakeAnim.getKeyFrame(stateTimer);
        }

        // Determine horizontal orientation directly from the physical body's velocity vector
        float velX = mossfly.b2body.getLinearVelocity().x;

        if (velX > 0 && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (velX < 0 && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        // Calculate rendering coordinates and apply state-dependent vertical offsets
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = mossfly.b2body.getPosition().x - (width / 2f);
        float yOffsetCoefficient = currentState == State.DEAD ? 0.5f : 1.75f;
        float y = mossfly.b2body.getPosition().y - (height / 2f) + Constants.Enemy.Mossfly.RADIUS_HALVED_SCALED * yOffsetCoefficient;

        batch.draw(currentFrame, x, y, width, height);
    }

    private enum State {HIDDEN, APPEARING, FLYING, DEAD}
}
