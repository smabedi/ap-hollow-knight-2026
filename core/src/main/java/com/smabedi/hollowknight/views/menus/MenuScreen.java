package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;

abstract public class MenuScreen implements Screen {
    protected Stage stage;
    protected Skin skin;

    abstract public void showCore();

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        stage = new Stage(viewport);
        skin = Assets.getSkin();
        showCore();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
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
