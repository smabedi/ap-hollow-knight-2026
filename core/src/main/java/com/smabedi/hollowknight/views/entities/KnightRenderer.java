package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.knight.KnightState;

/**
 * Primary rendering engine for the player character.
 * Maps the discrete logical states of the Knight's physics model to corresponding
 * visual animation frames, while managing transient overlays like weapon slash VFX.
 */
public class KnightRenderer implements EntityRenderer {
    private final Knight player;
    private final Animation<TextureRegion> idleAnim, runAnim, jumpAnim, fallAnim, doubleJumpAnim;
    private final Animation<TextureRegion> dashAnim, shadowDashAnim, wallSlideAnim;
    private final Animation<TextureRegion> attackSideAnim, attackDownAnim, attackUpAnim, focusAnim;
    private final Animation<TextureRegion> castWraithsAnim, castVoidWraithsAnim, castSpiritAnim, castVoidSpiritAnim;
    private final Animation<TextureRegion> sideSlashVfx, downSlashVfx;
    private float stateTimer = 0f;
    private KnightState lastRenderedState = KnightState.IDLE;
    private Animation<TextureRegion> activeVfx = null;
    private float vfxTimer = 0f;
    private boolean vfxFacingRight = true;
    private float lastFocusTimer = 0f;

    public KnightRenderer(Knight player, TextureAtlas atlas) {
        this.player = player;

        // Initialize and configure all animation sequences from the designated texture atlas
        idleAnim = new Animation<>(0.1f, atlas.findRegions("knight_idle_still"), Animation.PlayMode.LOOP);
        runAnim = new Animation<>(0.1f, atlas.findRegions("knight_run"), Animation.PlayMode.LOOP);
        jumpAnim = new Animation<>(0.1f, atlas.findRegions("knight_jump"), Animation.PlayMode.NORMAL);
        fallAnim = new Animation<>(0.1f, atlas.findRegions("knight_fall"), Animation.PlayMode.LOOP);
        doubleJumpAnim = new Animation<>(0.1f, atlas.findRegions("knight_double_jump_v02"), Animation.PlayMode.NORMAL);
        dashAnim = new Animation<>(0.1f, atlas.findRegions("knight_dash_v02"), Animation.PlayMode.NORMAL);
        shadowDashAnim = new Animation<>(0.05f, atlas.findRegions("knight_shadow_dash"), Animation.PlayMode.NORMAL);
        wallSlideAnim = new Animation<>(0.1f, atlas.findRegions("knight_wall_slide"), Animation.PlayMode.LOOP);
        focusAnim = new Animation<>(0.1f, atlas.findRegions("knight_collect_normal"), Animation.PlayMode.LOOP);

        attackSideAnim = new Animation<>(0.075f, atlas.findRegions("knight_charge_slash"), Animation.PlayMode.NORMAL);
        attackDownAnim = new Animation<>(0.1f, atlas.findRegions("knight_down_slash_v0"), Animation.PlayMode.NORMAL);
        attackUpAnim = new Animation<>(0.075f, atlas.findRegions("knight_up_slash"), Animation.PlayMode.NORMAL);
        sideSlashVfx = new Animation<>(0.04f, atlas.findRegions("knight_charge_slash_effect"), Animation.PlayMode.NORMAL);
        downSlashVfx = new Animation<>(0.075f, atlas.findRegions("knight_down_slash_effect"), Animation.PlayMode.NORMAL);

        castWraithsAnim = new Animation<>(0.1f, atlas.findRegions("knight_cast"), Animation.PlayMode.NORMAL);
        castVoidWraithsAnim = new Animation<>(0.1f, atlas.findRegions("knight_scream_cast_lvl"), Animation.PlayMode.NORMAL);
        castSpiritAnim = new Animation<>(0.1f, atlas.findRegions("knight_cast_v03"), Animation.PlayMode.NORMAL);
        castVoidSpiritAnim = new Animation<>(0.1f, atlas.findRegions("knight_cast_level"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void render(Batch batch, float dt) {
        // Detect if state changed OR if a continuous focus cycle restarted (focusTimer reset to 0)
        boolean focusRestarted = (player.currentState == KnightState.FOCUSING && player.focusTimer < lastFocusTimer);

        if (player.currentState != lastRenderedState || focusRestarted) {
            stateTimer = 0;
            if (player.currentState == KnightState.ATTACKING_SIDE) triggerVfx(sideSlashVfx);
            if (player.currentState == KnightState.ATTACKING_DOWN) triggerVfx(downSlashVfx);
            if (player.currentState == KnightState.ATTACKING_UP) triggerVfx(downSlashVfx);
        } else {
            stateTimer += dt;
        }
        lastRenderedState = player.currentState;
        lastFocusTimer = player.focusTimer;

        if (activeVfx != null) vfxTimer += dt;

        TextureRegion currentFrame = getAnimForState(player.currentState).getKeyFrame(stateTimer);

        // Resolves horizontal sprite orientation based on spellcasting states and wall-sliding physical constraints
        boolean isFlipped = (player.currentState == KnightState.CASTING_WRAITHS || player.currentState == KnightState.CASTING_VOID_WRAITHS);

        if (player.currentState == KnightState.WALL_SLIDING) {
            if (player.isTouchingRightWall && !currentFrame.isFlipX()) currentFrame.flip(true, false);
            else if (player.isTouchingLeftWall && currentFrame.isFlipX()) currentFrame.flip(true, false);
        } else if (isFlipped) {
            if (!player.facingRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
            else if (player.facingRight && currentFrame.isFlipX()) currentFrame.flip(true, false);
        } else {
            if (!player.facingRight && currentFrame.isFlipX()) currentFrame.flip(true, false);
            else if (player.facingRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        }

        // Computes centralized rendering coordinates aligned with the underlying Box2D fixture bounds
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = player.b2body.getPosition().x - (width / 2f);
        float y = player.b2body.getPosition().y - (height / 2f) + 0.5f;

        // Applies alpha modulation to visually indicate active invincibility frames (I-Frames)
        if (player.iFrameTimer > 0 && !player.isDashing && (player.iFrameTimer % 0.3f) > 0.1f) {
            batch.setColor(1, 1, 1, 0.1f);
        } else {
            batch.setColor(1, 1, 1, 1f);
        }

        batch.draw(currentFrame, x, y, width, height);
        batch.setColor(1, 1, 1, 1f);

        // Evaluates and renders transient visual effect overlays, such as directional weapon slashes
        if (activeVfx != null) {
            if (!activeVfx.isAnimationFinished(vfxTimer)) {
                TextureRegion vfxFrame = activeVfx.getKeyFrame(vfxTimer);

                float vfxWidth = vfxFrame.getRegionWidth() / Constants.World.PPM;
                float vfxHeight = vfxFrame.getRegionHeight() / Constants.World.PPM;
                float vfxX = player.b2body.getPosition().x;
                float vfxY = player.b2body.getPosition().y;

                // Ensure VFX flipping accurately tracks the player's orientation at the moment of execution
                boolean drawFlipX = vfxFacingRight;
                boolean drawFlipY = false;

                // Calibrate the visual effect coordinates dynamically to the player's center of mass
                vfxX -= (vfxWidth / 2f);
                vfxY -= (vfxHeight / 2f);

                // Apply targeted offsets based on the distinct geometry of downward versus lateral attacks
                if (player.isAttackingDown) {
                    vfxY -= Constants.Knight.HEIGHT_HALVED_SCALED;
                } else if (player.isAttackingUp) {
                    vfxY += Constants.Knight.HEIGHT_HALVED_SCALED * 1.5f; // Position above the Knight's head
                    drawFlipY = true;
                } else {
                    float sideOffsetX = 1f;
                    vfxX += vfxFacingRight ? sideOffsetX : -sideOffsetX;
                    drawFlipY = true;
                }

                // Utilize the overloaded draw method to manipulate rotation and mirroring without mutating the source TextureRegion
                batch.draw(vfxFrame.getTexture(),
                    vfxX, vfxY,
                    vfxWidth / 2f, vfxHeight / 2f,
                    vfxWidth, vfxHeight,
                    1f, 1f, 0f,
                    vfxFrame.getRegionX(), vfxFrame.getRegionY(),
                    vfxFrame.getRegionWidth(), vfxFrame.getRegionHeight(),
                    drawFlipX, drawFlipY);
            } else {
                activeVfx = null;
            }
        }
    }

    private void triggerVfx(Animation<TextureRegion> vfx) {
        activeVfx = vfx;
        vfxTimer = 0f;
        vfxFacingRight = player.facingRight;
    }

    private Animation<TextureRegion> getAnimForState(KnightState state) {
        return switch (state) {
            case FOCUSING -> focusAnim;
            case CASTING_VOID_WRAITHS -> castVoidWraithsAnim;
            case CASTING_WRAITHS -> castWraithsAnim;
            case CASTING_VOID_SPIRIT -> castVoidSpiritAnim;
            case CASTING_SPIRIT -> castSpiritAnim;
            case ATTACKING_UP -> attackUpAnim;
            case ATTACKING_DOWN -> attackDownAnim;
            case ATTACKING_SIDE -> attackSideAnim;
            case SHADOW_DASHING -> shadowDashAnim;
            case DASHING -> dashAnim;
            case WALL_SLIDING -> wallSlideAnim;
            case DOUBLE_JUMPING -> doubleJumpAnim;
            case JUMPING -> jumpAnim;
            case FALLING -> fallAnim;
            case WALKING -> runAnim;
            default -> idleAnim;
        };
    }
}
