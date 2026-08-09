package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.enemies.BossMove;
import com.smabedi.hollowknight.models.entities.enemies.BossPhase;
import com.smabedi.hollowknight.models.entities.enemies.BossSubState;
import com.smabedi.hollowknight.models.entities.enemies.FalseKnight;

/**
 * Complex rendering controller for the False Knight boss entity.
 * Translates a multi-dimensional AI state (Phase, Move, SubState) into a singular
 * visual animation frame, and dynamically manipulates time-deltas to accelerate
 * visual playback during the second combat phase.
 */
public class FalseKnightRenderer implements EntityRenderer {
    private final FalseKnight boss;
    private final Animation<TextureRegion> idleAnim, runAnim, jumpAnim, jumpAttackAnim, landAnim;
    private final Animation<TextureRegion> attackAnim, anticipateAnim, recoverAnim;
    private final Animation<TextureRegion> stunIdleAnim, stunHitAnim, stunRecoverAnim, deathAnim;
    private float stateTimer = 0f;
    private VisualState currentVisualState = VisualState.IDLE;

    public FalseKnightRenderer(FalseKnight boss, TextureAtlas atlas) {
        this.boss = boss;
        idleAnim = new Animation<>(0.1f, atlas.findRegions("boss_idle"), Animation.PlayMode.LOOP);
        runAnim = new Animation<>(0.1f, atlas.findRegions("boss_run"), Animation.PlayMode.LOOP);
        jumpAnim = new Animation<>(0.1f, atlas.findRegions("boss_jump"), Animation.PlayMode.NORMAL);
        jumpAttackAnim = new Animation<>(0.1f, atlas.findRegions("boss_jump_attack"), Animation.PlayMode.NORMAL);
        landAnim = new Animation<>(0.1f, atlas.findRegions("boss_land"), Animation.PlayMode.NORMAL);
        attackAnim = new Animation<>(0.1f, atlas.findRegions("boss_attack"), Animation.PlayMode.NORMAL);
        anticipateAnim = new Animation<>(0.1f, atlas.findRegions("boss_attack_anticipate"), Animation.PlayMode.NORMAL);
        recoverAnim = new Animation<>(0.1f, atlas.findRegions("boss_attack_recover"), Animation.PlayMode.NORMAL);
        stunIdleAnim = new Animation<>(0.1f, atlas.findRegions("boss_open_armor"), Animation.PlayMode.LOOP);
        stunHitAnim = new Animation<>(0.1f, atlas.findRegions("boss_open_armor_hit"), Animation.PlayMode.NORMAL);
        stunRecoverAnim = new Animation<>(0.1f, atlas.findRegions("boss_stun_recover"), Animation.PlayMode.NORMAL);
        deathAnim = new Animation<>(0.1f, atlas.findRegions("boss_death"), Animation.PlayMode.NORMAL);
    }

    public void render(Batch batch, float dt) {
        VisualState nextState = determineVisualState();

        if (nextState != currentVisualState) {
            stateTimer = 0f;
            currentVisualState = nextState;
        }

        // Dynamically multiply the delta injected into the animation timer during the second phase.
        // This ensures the visual frames remain perfectly synchronized with the accelerated Box2D physics engine.
        float speedMultiplier = (boss.getCurrentPhase() == BossPhase.PHASE_2) ? 1.75f : 1.0f;
        stateTimer += (dt * speedMultiplier);

        TextureRegion currentFrame = getFrame(currentVisualState, stateTimer);

        boolean faceRight = boss.facingRight;
        if (faceRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        else if (!faceRight && currentFrame.isFlipX()) currentFrame.flip(true, false);

        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = boss.b2body.getPosition().x - (width / 2f);
        float y = boss.b2body.getPosition().y - (height / 2f) + Constants.FalseKnight.HEIGHT_HALVED_SCALED * 0.8f;

        batch.draw(currentFrame, x, y, width, height);
    }

    /**
     * Resolves the multi-dimensional AI state machine into a linear visual state enum.
     */
    private VisualState determineVisualState() {
        if (boss.isDead()) return VisualState.DEATH;

        if (boss.getCurrentPhase() == BossPhase.STUNNED) {
            if (boss.getCurrentSubState() == BossSubState.STUN_HIT) return VisualState.STUN_HIT;
            if (boss.getCurrentSubState() == BossSubState.RECOVERY) return VisualState.STUN_RECOVER;
            return VisualState.STUN_IDLE;
        }

        BossMove move = boss.getCurrentMove();
        BossSubState sub = boss.getCurrentSubState();

        if (move == BossMove.IDLE) return VisualState.IDLE;

        if (move == BossMove.CHARGE) {
            if (sub == BossSubState.WIND_UP) return VisualState.IDLE;
            if (sub == BossSubState.ACTIVE) return VisualState.RUN;
            return VisualState.RECOVER;
        }

        // Isolate specific attack maneuvers that necessitate a wind-up anticipation frame
        if (move == BossMove.MACE_SLAM || move == BossMove.POWER_SLAM) {
            if (sub == BossSubState.WIND_UP) return VisualState.ANTICIPATE;
            if (sub == BossSubState.ACTIVE) {
                if (boss.b2body.getLinearVelocity().y <= 0.1f) return VisualState.JUMP_ATTACK;
                return VisualState.JUMP;
            }
            if (sub == BossSubState.ATTACK) return VisualState.ATTACK;
            if (sub == BossSubState.RECOVERY) return VisualState.RECOVER;
        }

        if (move == BossMove.OFFENSIVE_LEAP || move == BossMove.DEFENSIVE_LEAP) {
            if (sub == BossSubState.WIND_UP) return VisualState.IDLE;
            if (sub == BossSubState.ACTIVE) return VisualState.JUMP;
            if (sub == BossSubState.RECOVERY) return VisualState.LAND;
        }

        return VisualState.IDLE;
    }

    private TextureRegion getFrame(VisualState state, float timer) {
        return switch (state) {
            case RUN -> runAnim.getKeyFrame(timer);
            case JUMP -> jumpAnim.getKeyFrame(timer);
            case JUMP_ATTACK -> jumpAttackAnim.getKeyFrame(timer);
            case LAND -> landAnim.getKeyFrame(timer);
            case ATTACK -> attackAnim.getKeyFrame(timer);
            case ANTICIPATE -> anticipateAnim.getKeyFrame(timer);
            case RECOVER -> recoverAnim.getKeyFrame(timer);
            case STUN_IDLE -> stunIdleAnim.getKeyFrame(timer);
            case STUN_HIT -> stunHitAnim.getKeyFrame(timer);
            case STUN_RECOVER -> stunRecoverAnim.getKeyFrame(timer);
            case DEATH -> deathAnim.getKeyFrame(timer);
            default -> idleAnim.getKeyFrame(timer);
        };
    }

    private enum VisualState {
        IDLE, RUN, JUMP, JUMP_ATTACK, LAND, ATTACK, ANTICIPATE, RECOVER,
        STUN_IDLE, STUN_HIT, STUN_RECOVER, DEATH
    }
}
