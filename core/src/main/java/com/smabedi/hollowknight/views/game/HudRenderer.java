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

public class HudRenderer {
    private final Knight player;
    private final ExtendViewport uiViewport;
    private final OrthographicCamera uiCamera;

    // Assets
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
    // Soul tracking state
    private float stateTime = 0f;
    private float currentFillPercent = 0f;
    public HudRenderer(Knight player) {
        this.player = player;

        // 1. Separate UI camera setup
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new ExtendViewport(Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT, uiCamera);

        // 2. Load Textures directly from your HUD Atlas
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

        // 3. FBO Configuration
        int fboW = baseBar.getRegionWidth();
        int fboH = baseBar.getRegionHeight();
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboW, fboH, false);
        this.fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        this.fboRegion.flip(false, true);

        this.fboCamera = new OrthographicCamera(fboW, fboH);
        this.fboCamera.position.set(fboW / 2f, fboH / 2f, 0);
        this.fboCamera.update();

        // 4. Initialize Mask States
        int maxHealth = Constants.Knight.MAX_HEALTH;
        this.maskStates = new MaskState[maxHealth];
        this.maskTimers = new float[maxHealth];
        for (int i = 0; i < maxHealth; i++) {
            maskStates[i] = (player.health > i) ? MaskState.FULL : MaskState.EMPTY;
        }
    }

    public void update(float dt) {
        stateTime += dt;

        // Soul Lerping
        float targetPercent = player.soul / (float) Constants.Knight.MAX_SOUL;
        if (currentFillPercent < targetPercent) {
            currentFillPercent = Math.min(targetPercent, currentFillPercent + dt * 1.5f);
        } else if (currentFillPercent > targetPercent) {
            currentFillPercent = Math.max(targetPercent, currentFillPercent - dt * 1.5f);
        }

        // Mask State Transitions
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

    public void render(SpriteBatch batch) {
        // --- 1. PRE-RENDER FBO MASK IN AN ISOLATED PIPELINE ---
        batch.end();

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
        float liquidY = -100f + 1.02f * (100f * currentFillPercent); // Min Y is -100, Max Y is 0

        batch.draw(liquidFrame, 10, liquidY);
        batch.draw(eyes, 0, 0);

        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
        batch.draw(maskCircle, 0, 0);

        batch.end();
        fbo.end();

        // --- 2. MAIN HUD RENDERING ON TOP-LEFT CORNER ---
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Layout Calculations (Top-Left Anchors)
        float padding = 30f;
        float hudX = padding;
        float hudY = uiViewport.getWorldHeight() - baseBar.getRegionHeight() - padding;

        // Draw Base, Masked FBO result, and Glass Shine
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

    public void dispose() {
        if (fbo != null) fbo.dispose();
    }

    // Mask tracking state
    private enum MaskState {FULL, EMPTY, BREAKING, HEALING}
}
