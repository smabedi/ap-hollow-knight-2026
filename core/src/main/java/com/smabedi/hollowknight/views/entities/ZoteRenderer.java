package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.npcs.Zote;

/**
 * Dedicated renderer for the NPC Zote.
 * Evaluates dynamic interaction flags and hostile states to determine the correct
 * visual playback sequence.
 */
public class ZoteRenderer {
    private final Zote zote;
    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> talkAnim;
    private final Animation<TextureRegion> attackAnim;
    private float stateTimer = 0f;
    private VisualState currentVisualState = VisualState.IDLE;

    public ZoteRenderer(Zote zote, TextureAtlas atlas) {
        this.zote = zote;
        idleAnim = new Animation<>(0.1f, atlas.findRegions("zote_idle"), Animation.PlayMode.LOOP);
        // Normal playback ensures the dialogue animation completes a single cycle per interaction
        talkAnim = new Animation<>(0.1f, atlas.findRegions("zote_talk"), Animation.PlayMode.NORMAL);
        attackAnim = new Animation<>(0.1f, atlas.findRegions("zote_attack"), Animation.PlayMode.LOOP);
    }

    public void render(Batch batch, float dt) {
        VisualState nextState = currentVisualState;

        // Map the logical NPC interaction states to the visual rendering enumerator
        if (zote.isAngry()) {
            nextState = VisualState.ATTACK;
        } else if (zote.isTalking()) {
            // Intercept active dialogue flags to trigger the talking animation sequence
            if (currentVisualState != VisualState.TALK) {
                nextState = VisualState.TALK;
            }
        } else if (currentVisualState == VisualState.TALK) {
            // Lock the visual state machine until the transient talking animation resolves completely
            if (talkAnim.isAnimationFinished(stateTimer)) {
                nextState = VisualState.IDLE;
            }
        } else {
            nextState = VisualState.IDLE;
        }

        // Reset the internal timer upon state transitions to ensure clean animation playback
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

        // Synchronize horizontal sprite orientation with the NPC's movement trajectory
        boolean faceRight = zote.isFacingRight();
        if (faceRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        else if (!faceRight && currentFrame.isFlipX()) currentFrame.flip(true, false);

        // Calculate and apply centralized rendering coordinates based on the physical body's position
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = zote.b2body.getPosition().x - (width / 2f);
        float y = zote.b2body.getPosition().y - (height / 2f) + Constants.Zote.HEIGHT_HALVED_SCALED * 2f;

        batch.draw(currentFrame, x, y, width, height);
    }

    private enum VisualState {IDLE, TALK, ATTACK}
}
