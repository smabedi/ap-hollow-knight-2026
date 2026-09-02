package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;

/**
 * Abstract base class for all Scene2D-driven menu interfaces.
 * Provides a unified Stage viewport, shared UI Skin definitions, and handles
 * the rendering pipeline for dynamic, animated background themes.
 */
abstract public class MenuScreen implements Screen {
    protected final Stage stage;
    protected final Skin skin;
    private final Animation<TextureRegion> bgAnimation;
    private float stateTime = 0f;

    public MenuScreen() {
        FitViewport viewport = new FitViewport(Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = Assets.getSkin();

        // Retrieve the user's persisted theme preference (e.g., "theme_void")
        String themeKey = GameSettings.getMenuTheme();

        // Parse the raw setting key to derive the exact TextureAtlas region identifier
        String regionName = themeKey.replace("theme_", "");

        // Instantiate the background animation to cycle at 10 frames per second
        this.bgAnimation = new Animation<>(0.1f, Assets.getBackgroundAtlas().findRegions(regionName), Animation.PlayMode.LOOP);
    }

    /**
     * Implementing classes must define their specific Scene2D actor layouts here.
     */
    abstract public void showCore();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();
        showCore();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;
        TextureRegion currentFrame = bgAnimation.getKeyFrame(stateTime, true);

        // Render the active background animation frame spanning the entire viewport
        stage.getBatch().begin();
        stage.getBatch().draw(
            currentFrame,
            0, 0,
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight()
        );
        stage.getBatch().end();

        // Step and render the Scene2D UI actors overlaid on top of the background
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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

    @Override
    public void dispose() {
        stage.dispose();
    }
}
