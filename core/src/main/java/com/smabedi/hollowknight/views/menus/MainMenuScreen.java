package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

/**
 * The primary entry interface for the application.
 * Utilizes a Scene2D Stack to decouple the layout constraints of the central logo
 * and options from the peripheral navigation buttons.
 */
public class MainMenuScreen extends MenuScreen {
    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        Table mainOptionsWrapper = new Table();
        mainOptionsWrapper.center();
        // Configure the main options wrapper without a global width constraint to allow the central logo to render at its native scaled dimensions.
        mainOptionsWrapper.defaults().spaceBottom(50);
        stack.add(mainOptionsWrapper);

        TextureRegion logoRegion = Assets.getUiAtlas().findRegion("logo");
        Image logoImage = new Image(logoRegion);

        // Inject and scale the primary studio logo, enforcing aspect ratio preservation via Scaling.fit.
        logoImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        // Constrain the logo cell bounds to prevent spatial conflicts with the primary navigation buttons.
        mainOptionsWrapper.add(logoImage).width(900).height(250).padBottom(60).row();

        TextButton startGameBtn = new TextButton(Assets.getString("start_game"), skin);
        mainOptionsWrapper.add(startGameBtn).width(300).row();

        startGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.START_GAME);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        TextButton achievementsBtn = new TextButton(Assets.getString("achievements"), skin);
        mainOptionsWrapper.add(achievementsBtn).width(300).row();

        achievementsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.ACHIEVEMENTS);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        TextButton quitGameBtn = new TextButton(Assets.getString("quit_game"), skin);
        mainOptionsWrapper.add(quitGameBtn).width(300).row();

        quitGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Isolate peripheral navigation buttons using decoupled Table wrappers within the parent Stack.
        Table guideBtnWrapper = new Table();
        guideBtnWrapper.top().left().pad(50);
        stack.add(guideBtnWrapper);

        TextButton guideBtn = new TextButton(Assets.getString("guide"), skin);
        guideBtnWrapper.add(guideBtn).width(150);

        guideBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.GUIDE);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        Table settingsBtnWrapper = new Table();
        settingsBtnWrapper.top().right().pad(50);
        stack.add(settingsBtnWrapper);

        TextButton settingsBtn = new TextButton(Assets.getString("settings"), skin);
        settingsBtnWrapper.add(settingsBtn).width(150);

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
    }
}
