package com.smabedi.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

public class SplashScreen implements Screen {
    private final SpriteBatch batch;
    private final Texture logoTexture;
    private final Viewport viewport;
    private float elapsedTime = 0f;
    private float scale = 0.95f; // Starts slightly zoomed out
    private float alpha = 1f;
    private final Sound splashSound;

    private boolean isLoaded = false;

    public SplashScreen() {
        batch = new SpriteBatch();
        logoTexture = new Texture(Gdx.files.internal(Constants.Paths.Textures.SPLASH_LOGO));
        viewport = new FitViewport(Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT);
        splashSound = Gdx.audio.newSound(Gdx.files.internal(Constants.Paths.Sounds.SPLASH));

        // Play immediately if SFX are unmuted
        if (com.smabedi.hollowknight.config.GameSettings.shouldPlaySFX()) {
            splashSound.play(1.0f); // Plays at full volume
        }

        Assets.queueAssets();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        elapsedTime += delta;

        // 1. The Zoom Effect: Slowly increase the scale every frame
        scale += delta * 0.03f;

        // 2. The Loading Check
        if (!isLoaded) {
            // manager.update() returns true ONLY when every single asset is fully loaded into memory
            // Forces the logo to stay visible for at least 2 seconds
            float MINIMUM_SHOW_TIME = 1.5f;
            if (Assets.getInstance().manager.update() && elapsedTime >= MINIMUM_SHOW_TIME) {
                isLoaded = true;
            }
        } else {
            // 3. The Fade Out Effect
            alpha -= delta * 1.5f; // Fade speed

            if (alpha <= 0) {
                // Once completely invisible, move to the main menu!
                ScreenManager.setMenuScreen(ScreenType.MAIN);
                return; // Exit render loop
            }
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        // Apply the fading transparency
        batch.setColor(1, 1, 1, Math.max(alpha, 0));

        float texWidth = logoTexture.getWidth();
        float texHeight = logoTexture.getHeight();

        // 1. Calculate the proper base size to fit inside the viewport perfectly
        Vector2 baseSize = Scaling.fit.apply(texWidth, texHeight, Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT);

        // 2. Scale the base size down so it looks like a tasteful studio logo, not a wallpaper.
        float logoScaleModifier = 1f;
        float drawWidth = baseSize.x * logoScaleModifier;
        float drawHeight = baseSize.y * logoScaleModifier;

        // 3. Center it on the screen
        float x = (Constants.UI.DEFAULT_WIDTH - drawWidth) / 2f;
        float y = (Constants.UI.DEFAULT_HEIGHT - drawHeight) / 2f;

        // 4. Draw it, applying the dynamic zoom 'scale' on top of our new base size
        batch.draw(
            logoTexture,
            x, y,
            drawWidth / 2f, drawHeight / 2f, // Origin for the zoom scaling
            drawWidth, drawHeight,
            scale, scale, 0,
            0, 0, (int)texWidth, (int)texHeight, false, false
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
