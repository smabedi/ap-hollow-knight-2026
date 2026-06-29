package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.views.ScreenType;
import com.smabedi.hollowknight.views.ScreenManager;

public class MainMenuScreen extends MenuScreen {
    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        Table mainOptionsWrapper = new Table();
        mainOptionsWrapper.center().bottom().pad(300);
        mainOptionsWrapper.defaults().width(300).spaceBottom(50);
        stack.add(mainOptionsWrapper);

        TextButton startGameBtn = new TextButton(Assets.getString("start_game"), skin);
        mainOptionsWrapper.add(startGameBtn).row();

        startGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.START_GAME);
            }
        });

        TextButton achievementsBtn = new TextButton(Assets.getString("achievements"), skin);
        mainOptionsWrapper.add(achievementsBtn).row();

        TextButton quitGameBtn = new TextButton(Assets.getString("quit_game"), skin);
        mainOptionsWrapper.add(quitGameBtn).row();

        quitGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Table guideBtnWrapper = new Table();
        guideBtnWrapper.top().left().pad(50);
        stack.add(guideBtnWrapper);

        TextButton guideBtn = new TextButton(Assets.getString("guide"), skin);
        guideBtnWrapper.add(guideBtn).width(200);

        Table settingsBtnWrapper = new Table();
        settingsBtnWrapper.top().right().pad(50);
        stack.add(settingsBtnWrapper);

        TextButton settingsBtn = new TextButton(Assets.getString("settings"), skin);
        settingsBtnWrapper.add(settingsBtn).width(200);

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
            }
        });
    }
}
