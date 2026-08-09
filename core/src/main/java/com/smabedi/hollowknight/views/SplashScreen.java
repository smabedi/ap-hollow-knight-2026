package com.smabedi.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;

/**
 * The initial boot sequence view.
 * Displays a scalable, fading studio logo while synchronously queuing
 * and loading all heavyweight resources into the AssetManager.
 */
public class SplashScreen implements Screen {
    private final SpriteBatch batch;
    private final Texture logoTexture;
    private final Viewport viewport;
    private final Sound splashSound;
    private float elapsedTime = 0f;
    private float scale = 0.95f;
    private float alpha = 1f;
    private boolean isLoaded = false;

    public SplashScreen() {
        batch = new SpriteBatch();
        logoTexture = new Texture(Gdx.files.internal(Constants.Paths.Textures.SPLASH_LOGO));
        viewport = new FitViewport(Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT);
        splashSound = Gdx.audio.newSound(Gdx.files.internal(Constants.Paths.Sounds.SPLASH));

        if (com.smabedi.hollowknight.config.GameSettings.shouldPlaySFX()) {
            splashSound.play(1.0f);
        }

        Assets.queueAssets();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        elapsedTime += delta;
        scale += delta * 0.03f;

        if (!isLoaded) {
            float MINIMUM_SHOW_TIME = 1.5f;
            if (Assets.getInstance().manager.update() && elapsedTime >= MINIMUM_SHOW_TIME) {
                isLoaded = true;
            }
        } else {
            alpha -= delta * 1.5f;

            if (alpha <= 0) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
                return;
            }
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.setColor(1, 1, 1, Math.max(alpha, 0));

        float texWidth = logoTexture.getWidth();
        float texHeight = logoTexture.getHeight();

        Vector2 baseSize = Scaling.fit.apply(texWidth, texHeight, Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT);

        float logoScaleModifier = 1f;
        float drawWidth = baseSize.x * logoScaleModifier;
        float drawHeight = baseSize.y * logoScaleModifier;

        float x = (Constants.UI.DEFAULT_WIDTH - drawWidth) / 2f;
        float y = (Constants.UI.DEFAULT_HEIGHT - drawHeight) / 2f;

        batch.draw(
            logoTexture,
            x, y,
            drawWidth / 2f, drawHeight / 2f,
            drawWidth, drawHeight,
            scale, scale, 0,
            0, 0, (int) texWidth, (int) texHeight, false, false
        );
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        logoTexture.dispose();
        splashSound.dispose();
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
