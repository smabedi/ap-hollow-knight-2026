package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.knight.Knight;

/**
 * Dedicated rendering engine for the player's primary Heads Up Display (HUD).
 * Implements a specialized FrameBuffer Object (FBO) multi-pass rendering pipeline to
 * achieve complex alpha-masking operations for the dynamic soul orb liquid filler.
 */
public class HudRenderer {
    private final Knight player;
    private final ExtendViewport uiViewport;
    private final OrthographicCamera uiCamera;

    private final TextureRegion baseBar;
    private final TextureRegion maskCircle;
    private final TextureRegion eyes;
    private final TextureRegion glass;
    private final TextureRegion maskFull;
    private final TextureRegion maskEmpty;

    private final Animation<TextureRegion> orbIdle;
    private final Animation<TextureRegion> orbGrow;
    private final Animation<TextureRegion> orbShrink;
    private final Animation<TextureRegion> maskBreakAnim;
    private final Animation<TextureRegion> maskHealAnim;
    private final FrameBuffer fbo;
    private final TextureRegion fboRegion;
    private final OrthographicCamera fboCamera;
    private final MaskState[] maskStates;
    private final float[] maskTimers;
    private float stateTime = 0f;
    private float currentFillPercent = 0f;

    public HudRenderer(Knight player) {
        this.player = player;

        // Establish a decoupled orthographic camera for resolution-independent HUD rendering
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new ExtendViewport(Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT, uiCamera);

        TextureAtlas atlas = Assets.getUiAtlas();
        this.baseBar = atlas.findRegion("healthbar");
        this.maskCircle = atlas.findRegion("healthbar_mask");
        this.eyes = atlas.findRegion("healthbar_eyes");
        this.glass = atlas.findRegion("healthbar_glass");
        this.maskFull = atlas.findRegion("mask_full");
        this.maskEmpty = atlas.findRegion("mask_empty");

        this.orbIdle = new Animation<>(0.1f, atlas.findRegions("soulorb_idle"), Animation.PlayMode.LOOP);
        this.orbGrow = new Animation<>(0.1f, atlas.findRegions("soulorb_grow"), Animation.PlayMode.LOOP);
        this.orbShrink = new Animation<>(0.1f, atlas.findRegions("soulorb_shrink"), Animation.PlayMode.LOOP);
        this.maskBreakAnim = new Animation<>(0.05f, atlas.findRegions("mask_break"), Animation.PlayMode.NORMAL);
        this.maskHealAnim = new Animation<>(0.05f, atlas.findRegions("mask_heal"), Animation.PlayMode.NORMAL);

        // Configure the FrameBuffer Object (FBO) for the isolated soul orb alpha-masking operations
        int fboW = baseBar.getRegionWidth();
        int fboH = baseBar.getRegionHeight();
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboW, fboH, false);
        this.fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        this.fboRegion.flip(false, true);

        this.fboCamera = new OrthographicCamera(fboW, fboH);
        this.fboCamera.position.set(fboW / 2f, fboH / 2f, 0);
        this.fboCamera.update();

        // Pre-allocate tracking arrays for discrete health mask state evaluation
        int maxHealth = Constants.Knight.MAX_HEALTH;
        this.maskStates = new MaskState[maxHealth];
        this.maskTimers = new float[maxHealth];
        for (int i = 0; i < maxHealth; i++) {
            maskStates[i] = (player.health > i) ? MaskState.FULL : MaskState.EMPTY;
        }
    }

    /**
     * Steps logic for UI animations independently of physical game time.
     * Linearly interpolates resource gauges and manages discrete health state transitions.
     */
    public void update(float dt) {
        stateTime += dt;

        // Linearly interpolate the soul meter fill percentage for smooth visual transitions
        float targetPercent = player.soul / (float) Constants.Knight.MAX_SOUL;
        if (currentFillPercent < targetPercent) {
            currentFillPercent = Math.min(targetPercent, currentFillPercent + dt * 1.5f);
        } else if (currentFillPercent > targetPercent) {
            currentFillPercent = Math.max(targetPercent, currentFillPercent - dt * 1.5f);
        }

        // Evaluate state transitions for health masks (healing or breaking animations)
        for (int i = 0; i < maskStates.length; i++) {
            maskTimers[i] += dt;
            boolean shouldBeFull = player.health > i;

            if (shouldBeFull && (maskStates[i] == MaskState.EMPTY || maskStates[i] == MaskState.BREAKING)) {
                maskStates[i] = MaskState.HEALING;
                maskTimers[i] = 0f;
            } else if (!shouldBeFull && (maskStates[i] == MaskState.FULL || maskStates[i] == MaskState.HEALING)) {
                maskStates[i] = MaskState.BREAKING;
                maskTimers[i] = 0f;
            }

            if (maskStates[i] == MaskState.BREAKING && maskBreakAnim.isAnimationFinished(maskTimers[i])) {
                maskStates[i] = MaskState.EMPTY;
            } else if (maskStates[i] == MaskState.HEALING && maskHealAnim.isAnimationFinished(maskTimers[i])) {
                maskStates[i] = MaskState.FULL;
            }
        }
    }

    /**
     * Executes the two-pass rendering sequence. Processes complex masking operations into
     * the FBO before compositing the final HUD overlay onto the primary SpriteBatch.
     */
    public void render(SpriteBatch batch) {
        batch.end();

        // Pipeline Pass 1: Render the dynamic soul liquid masked by the orb boundary into the FBO
        fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(fboCamera.combined);
        batch.begin();

        Animation<TextureRegion> currentAnim = orbIdle;
        float targetPercent = player.soul / (float) Constants.Knight.MAX_SOUL;
        if (currentFillPercent < targetPercent - 0.01f) currentAnim = orbGrow;
        else if (currentFillPercent > targetPercent + 0.01f) currentAnim = orbShrink;

        TextureRegion liquidFrame = currentAnim.getKeyFrame(stateTime);

        // Calculate the vertical physical offset of the liquid texture based on internal capacity
        float liquidY = -100f + 1.02f * (100f * currentFillPercent);

        batch.draw(liquidFrame, 10, liquidY);
        batch.draw(eyes, 0, 0);

        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
        batch.draw(maskCircle, 0, 0);

        batch.end();
        fbo.end();

        // Pipeline Pass 2: Composite the base UI, FBO layer, and health mask states onto the viewport
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float padding = 30f;
        float hudX = padding;
        float hudY = uiViewport.getWorldHeight() - baseBar.getRegionHeight() - padding;

        batch.draw(baseBar, hudX, hudY);
        batch.draw(fboRegion, hudX, hudY);
        batch.draw(glass, hudX, hudY);

        float maskStartX = hudX + baseBar.getRegionWidth() / 2f;
        float maskY = hudY + (baseBar.getRegionHeight() / 2f) - (maskFull.getRegionHeight() / 2f) + 15f;

        for (int i = 0; i < maskStates.length; i++) {
            TextureRegion maskFrame = switch (maskStates[i]) {
                case FULL -> maskFull;
                case EMPTY -> maskEmpty;
                case BREAKING -> maskBreakAnim.getKeyFrame(maskTimers[i]);
                case HEALING -> maskHealAnim.getKeyFrame(maskTimers[i]);
            };
            float currentMaskX = maskStartX + (i * maskFull.getRegionWidth() / 1.5f);
            batch.draw(maskFrame, currentMaskX, maskY);
        }
    }

    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    /**
     * Prevents memory leaks by freeing the FrameBuffer Object from native VRAM.
     */
    public void dispose() {
        if (fbo != null) fbo.dispose();
    }

    /**
     * Encapsulates the tracking states used for sequential health loss and recovery animations.
     */
    private enum MaskState {FULL, EMPTY, BREAKING, HEALING}
}
