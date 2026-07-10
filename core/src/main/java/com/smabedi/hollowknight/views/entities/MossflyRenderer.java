package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.enemies.Mossfly;

public class MossflyRenderer implements EntityRenderer {
    private final Mossfly mossfly;
    private float stateTimer = 0f;

    private enum State {HIDDEN, APPEARING, FLYING, DEAD}

    private State currentState = State.HIDDEN;

    private final Animation<TextureRegion> shakeAnim;
    private final Animation<TextureRegion> appearAnim;
    private final Animation<TextureRegion> flyAnim;
    private final Animation<TextureRegion> deathAnim;

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

        // 1. Determine the logical next state
        if (mossfly.isDead()) {
            nextState = State.DEAD;
        } else if (mossfly.isHidden()) {
            nextState = State.HIDDEN;
        } else if (currentState == State.HIDDEN) {
            // Just broke cover!
            nextState = State.APPEARING;
        } else if (currentState == State.APPEARING && appearAnim.isAnimationFinished(stateTimer)) {
            // Finished the transition animation, start flying
            nextState = State.FLYING;
        }

        // 2. Reset timer if state changed
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
            default -> currentFrame = shakeAnim.getKeyFrame(stateTimer); // HIDDEN
        }

        // 3. Flipping logic (Default asset faces LEFT)
        float velX = mossfly.b2body.getLinearVelocity().x;

        if (velX > 0 && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (velX < 0 && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        // 4. Draw
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = mossfly.b2body.getPosition().x - (width / 2f);
        float yOffsetCoefficient = currentState == State.DEAD ? 0.5f : 1.75f;
        float y = mossfly.b2body.getPosition().y - (height / 2f) + Constants.Enemy.Mossfly.RADIUS_HALVED_SCALED * yOffsetCoefficient;

        batch.draw(currentFrame, x, y, width, height);
    }
}
