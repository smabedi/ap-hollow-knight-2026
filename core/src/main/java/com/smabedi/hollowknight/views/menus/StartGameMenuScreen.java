package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.repositories.DatabaseManager;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;
import com.smabedi.hollowknight.views.customelements.SaveCard;

/**
 * Scene2D interface for managing game profiles.
 * Retrieves serialized GameSession objects from the local SQLite database
 * and dynamically constructs interactive UI widgets representing each save slot.
 */
public class StartGameMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        // Utilize a Stack layout to decouple the alignment of independent UI components
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

        // Execute queries to fetch persisted session states from the local database
        GameSession slot1Data = DatabaseManager.loadSession(1);
        GameSession slot2Data = DatabaseManager.loadSession(2);
        GameSession slot3Data = DatabaseManager.loadSession(3);
        GameSession slot4Data = DatabaseManager.loadSession(4);

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
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
    }
}
