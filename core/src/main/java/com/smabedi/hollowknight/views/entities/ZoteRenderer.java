package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.npcs.Zote;

public class ZoteRenderer {
    private final Zote zote;
    private float stateTimer = 0f;

    private enum VisualState { IDLE, TALK, ATTACK }
    private VisualState currentVisualState = VisualState.IDLE;

    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> talkAnim;
    private final Animation<TextureRegion> attackAnim;

    public ZoteRenderer(Zote zote, TextureAtlas atlas) {
        this.zote = zote;
        idleAnim = new Animation<>(0.1f, atlas.findRegions("zote_idle"), Animation.PlayMode.LOOP);

        // NORMAL mode ensures it plays exactly once and stops!
        talkAnim = new Animation<>(0.1f, atlas.findRegions("zote_talk"), Animation.PlayMode.NORMAL);

        attackAnim = new Animation<>(0.1f, atlas.findRegions("zote_attack"), Animation.PlayMode.LOOP);
    }

    public void render(Batch batch, float dt) {
        VisualState nextState = currentVisualState;

        // 1. Determine State
        if (zote.isAngry()) {
            nextState = VisualState.ATTACK;
        }
        else if (zote.isTalking()) {
            // Model says we just interacted! Switch to TALK.
            if (currentVisualState != VisualState.TALK) {
                nextState = VisualState.TALK;
            }
        }
        else if (currentVisualState == VisualState.TALK) {
            // If we are currently talking visually, stay in this state UNTIL the animation finishes!
            if (talkAnim.isAnimationFinished(stateTimer)) {
                nextState = VisualState.IDLE;
            }
        }
        else {
            nextState = VisualState.IDLE;
        }

        // 2. State Transition Reset
        if (nextState != currentVisualState) {
            stateTimer = 0f;
            currentVisualState = nextState;
        }

        stateTimer += dt;
        TextureRegion currentFrame;

        switch (currentVisualState) {
            case ATTACK -> currentFrame = attackAnim.getKeyFrame(stateTimer);
            case TALK -> currentFrame = talkAnim.getKeyFrame(stateTimer);
            default -> currentFrame = idleAnim.getKeyFrame(stateTimer);
        }

        // 3. Flipping Logic (Zote assets face LEFT by default)
        boolean faceRight = zote.isFacingRight();
        if (faceRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        else if (!faceRight && currentFrame.isFlipX()) currentFrame.flip(true, false);

        // 4. Draw
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = zote.b2body.getPosition().x - (width / 2f);
        float y = zote.b2body.getPosition().y - (height / 2f) + Constants.Zote.HEIGHT_HALVED_SCALED * 2f;

        batch.draw(currentFrame, x, y, width, height);
    }
}
