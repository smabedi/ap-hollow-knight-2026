package com.smabedi.hollowknight.views.customelements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;

/**
 * A specialized Scene2D Actor that generates a static, miniature replica of the main game HUD.
 * Utilizes a localized FrameBuffer Object (FBO) pipeline upon initialization to bake complex
 * mask blending effects into a single lightweight texture, ensuring UI performance.
 */
public class SaveCardHudActor extends Actor {
    private final TextureRegion baseBar;
    private final TextureRegion glass;
    private final TextureRegion maskFull;
    private final Texture fboTex;
    private final TextureRegion fboRegion;

    private final int health;
    private final float scale = 0.6f;

    public SaveCardHudActor(int health, int soul) {
        this.health = health;

        TextureAtlas atlas = Assets.getUiAtlas();
        this.baseBar = atlas.findRegion("healthbar");
        this.glass = atlas.findRegion("healthbar_glass");
        this.maskFull = atlas.findRegion("mask_full");

        TextureRegion maskCircle = atlas.findRegion("healthbar_mask");
        TextureRegion eyes = atlas.findRegion("healthbar_eyes");
        TextureRegion orbIdle = atlas.findRegions("soulorb_idle").first();

        int fboW = baseBar.getRegionWidth();
        int fboH = baseBar.getRegionHeight();

        FrameBuffer tempFbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboW, fboH, false);
        SpriteBatch tempBatch = new SpriteBatch();
        OrthographicCamera fboCam = new OrthographicCamera(fboW, fboH);
        fboCam.position.set(fboW / 2f, fboH / 2f, 0);
        fboCam.update();
        tempBatch.setProjectionMatrix(fboCam.combined);

        tempFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tempBatch.begin();
        float fillPercent = soul / (float) Constants.Knight.MAX_SOUL;
        float liquidY = -100f + 1.02f * (100f * fillPercent);

        tempBatch.draw(orbIdle, 10, liquidY);
        tempBatch.draw(eyes, 0, 0);
        tempBatch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
        tempBatch.draw(maskCircle, 0, 0);
        tempBatch.end();

        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, fboW, fboH);
        tempFbo.end();

        this.fboTex = new Texture(pixmap);
        this.fboRegion = new TextureRegion(fboTex);
        this.fboRegion.flip(false, true);

        pixmap.dispose();
        tempFbo.dispose();
        tempBatch.dispose();

        float totalWidth = (fboW * scale) + (health * (maskFull.getRegionWidth() * scale / 1.5f));
        setSize(totalWidth, fboH * scale);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float drawX = getX();
        float drawY = getY();

        float w = baseBar.getRegionWidth() * scale;
        float h = baseBar.getRegionHeight() * scale;

        batch.draw(baseBar, drawX, drawY, w, h);
        batch.draw(fboRegion, drawX, drawY, w, h);
        batch.draw(glass, drawX, drawY, w, h);

        float maskStartX = drawX + w / 2f;
        float maskW = maskFull.getRegionWidth() * scale;
        float maskH = maskFull.getRegionHeight() * scale;
        float maskY = drawY + (h / 2f) - (maskH / 2f) + (15f * scale);

        for (int i = 0; i < health; i++) {
            float currentMaskX = maskStartX + (i * maskW / 1.5f);
            batch.draw(maskFull, currentMaskX, maskY, maskW, maskH);
        }
    }

    @Override
    public boolean remove() {
        dispose();
        return super.remove();
    }

    /**
     * Prevents memory leaks by releasing the dynamically generated texture from VRAM.
     */
    public void dispose() {
        if (fboTex != null) fboTex.dispose();
    }
}
