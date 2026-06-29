package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.ScreenType;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.customelements.SaveCard;

public class StartGameMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        Table titleWrapper = new Table();
        titleWrapper.top().padTop(50);
        Label titleLabel = new Label(Assets.getString("select_profile"), skin);
        titleWrapper.add(titleLabel);
        stack.add(titleWrapper);

        Table slotsWrapper = new Table();
        slotsWrapper.center();
        slotsWrapper.defaults().padBottom(20);
        stack.add(slotsWrapper);

        // Simulate fetching JSON data for the slots
        // TODO: Replace with actual JSON file parsing
        GameSession slot1Data = new GameSession(); // Pretend this loaded from save1.json
        GameSession slot2Data = null;              // Empty
        GameSession slot3Data = null;              // Empty
        GameSession slot4Data = null;              // Empty

        slotsWrapper.add(new SaveCard(1, slot1Data, skin)).row();
        slotsWrapper.add(new SaveCard(2, slot2Data, skin)).row();
        slotsWrapper.add(new SaveCard(3, slot3Data, skin)).row();
        slotsWrapper.add(new SaveCard(4, slot4Data, skin)).row();

        Table backBtnWrapper = new Table();
        backBtnWrapper.top().left().pad(50);
        stack.add(backBtnWrapper);

        TextButton backBtn = new TextButton(Assets.getString("back"), skin);
        backBtnWrapper.add(backBtn).width(200);

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });
    }
}
