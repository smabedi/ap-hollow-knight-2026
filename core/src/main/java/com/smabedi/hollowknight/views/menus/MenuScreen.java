package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;

abstract public class MenuScreen implements Screen {
    protected final Stage stage;
    protected final Skin skin;
    private final Animation<TextureRegion> bgAnimation;
    private float stateTime = 0f;

    public MenuScreen() {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        this.stage = new Stage(viewport);
        this.skin = Assets.getSkin();

        // 1. Fetch the saved theme key (e.g., "theme_void")
        String themeKey = GameSettings.getMenuTheme();

        // 2. Strip the prefix to match the exact atlas region names
        String regionName = themeKey.replace("theme_", "");

        // 3. Initialize the animation at 10fps (0.1f duration per frame)
        this.bgAnimation = new Animation<>(0.1f, Assets.getBackgroundAtlas().findRegions(regionName), Animation.PlayMode.LOOP);
    }

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

        // Render the background animation first
        stage.getBatch().begin();
        stage.getBatch().draw(
            currentFrame,
            0, 0,
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight()
        );
        stage.getBatch().end();

        // Render the UI elements on top
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
