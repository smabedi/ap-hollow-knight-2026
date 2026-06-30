package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

public class GameUI {
    public final Stage stage;
    private final Skin skin;
    private Table pauseMenu;
    private Table dialogBox;
    private Table toastContainer;

    public GameUI() {
        ScreenViewport viewport = new ScreenViewport(new OrthographicCamera());
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        stage = new Stage(viewport);
        skin = Assets.getSkin();

        buildPauseMenu();
    }

    private void buildPauseMenu() {
        pauseMenu = new Table();
        pauseMenu.setFillParent(true);
        pauseMenu.defaults().pad(10);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.7f));
        pixmap.fill();
        Texture transparentBlack = new Texture(pixmap);
        pauseMenu.setBackground(new TextureRegionDrawable(new TextureRegion(transparentBlack)));
        pixmap.dispose();

        Label title = new Label(Assets.getString("paused"), skin);
        pauseMenu.add(title).row();

        TextButton backBtn = new TextButton(Assets.getString("back"), skin);
        pauseMenu.add(backBtn).row();

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });

        // TODO: Add Resume, Settings, Quit buttons here.

        pauseMenu.setVisible(false);
        stage.addActor(pauseMenu);
    }

    private void buildDialogBox() {
        dialogBox = new Table();
        dialogBox.bottom().padBottom(50);
        dialogBox.setFillParent(true);

        // TODO: Add text labels her.

        dialogBox.setVisible(false);
        stage.addActor(dialogBox);
    }

    private void buildToastSystem() {
        toastContainer = new Table();
        toastContainer.top().right().pad(20);
        toastContainer.setFillParent(true);
        stage.addActor(toastContainer);
    }


    public void togglePause() {
        boolean isPaused = pauseMenu.isVisible();
        pauseMenu.setVisible(!isPaused);
        // TODO: Tell the PlayScreen's update() loop to stop stepping the world here.
    }

    public void showDialog(String text) {
        // TODO: Update label text and setVisible(true).
    }

    public void showToast(String message) {
        // TODO: Add a label to toastContainer, use Scene2D Actions to fade it out after 3 seconds.
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public boolean isPaused() {
        return pauseMenu != null && pauseMenu.isVisible();
    }
}
