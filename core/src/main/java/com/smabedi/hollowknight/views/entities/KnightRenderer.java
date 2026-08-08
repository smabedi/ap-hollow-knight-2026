package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.knight.KnightState;

public class KnightRenderer implements EntityRenderer {
    private final Knight player;
    private final Animation<TextureRegion> idleAnim, runAnim, jumpAnim, fallAnim, doubleJumpAnim;
    private final Animation<TextureRegion> dashAnim, shadowDashAnim, wallSlideAnim;
    private final Animation<TextureRegion> attackSideAnim, attackDownAnim, focusAnim;
    private final Animation<TextureRegion> castWraithsAnim, castVoidWraithsAnim, castSpiritAnim, castVoidSpiritAnim;
    private final Animation<TextureRegion> sideSlashVfx, downSlashVfx;
    private float stateTimer = 0f;
    private KnightState lastRenderedState = KnightState.IDLE;
    private Animation<TextureRegion> activeVfx = null;
    private float vfxTimer = 0f;
    private boolean vfxFacingRight = true;

    public KnightRenderer(Knight player, TextureAtlas atlas) {
        this.player = player;

        // Load all animations exactly as they were in the old Knight class...
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
        sideSlashVfx = new Animation<>(0.04f, atlas.findRegions("knight_charge_slash_effect"), Animation.PlayMode.NORMAL);
        downSlashVfx = new Animation<>(0.075f, atlas.findRegions("knight_down_slash_effect"), Animation.PlayMode.NORMAL);

        castWraithsAnim = new Animation<>(0.1f, atlas.findRegions("knight_cast"), Animation.PlayMode.NORMAL);
        castVoidWraithsAnim = new Animation<>(0.1f, atlas.findRegions("knight_scream_cast_lvl"), Animation.PlayMode.NORMAL);
        castSpiritAnim = new Animation<>(0.1f, atlas.findRegions("knight_cast_v03"), Animation.PlayMode.NORMAL);
        castVoidSpiritAnim = new Animation<>(0.1f, atlas.findRegions("knight_cast_level"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void render(Batch batch, float dt) {
        if (player.currentState != lastRenderedState) {
            stateTimer = 0;
            if (player.currentState == KnightState.ATTACKING_SIDE) triggerVfx(sideSlashVfx);
            if (player.currentState == KnightState.ATTACKING_DOWN) triggerVfx(downSlashVfx);
        } else {
            stateTimer += dt;
        }
        lastRenderedState = player.currentState;

        if (activeVfx != null) vfxTimer += dt;

        TextureRegion currentFrame = getAnimForState(player.currentState).getKeyFrame(stateTimer);

        // --- FLIPPING LOGIC ---
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

        // --- DRAWING LOGIC ---
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = player.b2body.getPosition().x - (width / 2f);
        float y = player.b2body.getPosition().y - (height / 2f) + 0.5f;

        // Blinking logic
        if (player.iFrameTimer > 0 && !player.isDashing && (player.iFrameTimer % 0.3f) > 0.1f) {
            batch.setColor(1, 1, 1, 0.1f); // Invisible
        } else {
            batch.setColor(1, 1, 1, 1f); // Visible
        }

        batch.draw(currentFrame, x, y, width, height);
        batch.setColor(1, 1, 1, 1f); // Always reset color

        // --- DRAW VFX OVERLAY (Weapon Slashes) ---
        if (activeVfx != null) {
            if (!activeVfx.isAnimationFinished(vfxTimer)) {
                TextureRegion vfxFrame = activeVfx.getKeyFrame(vfxTimer);

                float vfxWidth = vfxFrame.getRegionWidth() / Constants.World.PPM;
                float vfxHeight = vfxFrame.getRegionHeight() / Constants.World.PPM;
                float vfxX = player.b2body.getPosition().x;
                float vfxY = player.b2body.getPosition().y;

                // 1. Safe flipping: Default faces left, so we flip X if facing right
                boolean drawFlipX = vfxFacingRight;
                boolean drawFlipY = false;

                // 1. Center the VFX directly on the player first
                vfxX -= (vfxWidth / 2f);
                vfxY -= (vfxHeight / 2f);

                // 2. Apply simple directional offsets
                if (player.isAttackingDown) {
                    vfxY -= Constants.Knight.HEIGHT_HALVED_SCALED;
                } else {
                    float sideOffsetX = 1f; // Tweak this number to push it further out
                    vfxX += vfxFacingRight ? sideOffsetX : -sideOffsetX;
                    drawFlipY = true; // Safely flips upside down
                }

                // 3. Draw using the overloaded method that flips at render-time, avoiding Texture corruption
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
