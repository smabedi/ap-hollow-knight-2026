package com.smabedi.hollowknight.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.controllers.repositories.DatabaseManager;
import com.smabedi.hollowknight.views.ScreenManager;

public class SaveCard extends Table {
    private final int slotIndex;
    private GameSession session;
    private final Skin skin;

    public SaveCard(int slotIndex, GameSession session, Skin skin) {
        this.slotIndex = slotIndex;
        this.session = session;
        this.skin = skin;

        buildCard();
    }

    private void buildCard() {
        this.clearChildren();
        this.left().center();

        Label slotLabel = new Label(slotIndex + ".", skin);
        this.add(slotLabel).width(30);

        Button contentTable = new Button(skin);
        contentTable.left();

        if (session == null) {
            Label newGameLabel = new Label(Assets.getString("new_game"), skin);
            contentTable.add(newGameLabel).left().expandX();

            contentTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Create a fresh session and instantly commit it to the database
                    GameSession newSession = new GameSession(slotIndex);
                    DatabaseManager.saveSession(newSession);

                    ScreenManager.setGameScreen(newSession);
                }
            });
        } else {
            Label masksLabel = new Label(Assets.getString("mask") + ": " + session.health, skin);
            Label geoLabel = new Label(Assets.getString("soul") + ": " + session.soul, skin);
            Label locationLabel = new Label(session.location.getName(), skin);
            Label timeLabel = new Label(session.playtime + " " + Assets.getString("min"), skin);

            Table statsTable = new Table();
            statsTable.add(masksLabel).left().row();
            statsTable.add(geoLabel).left();
            contentTable.add(statsTable).padRight(50);

            Table infoTable = new Table();
            infoTable.add(locationLabel).right().row();
            infoTable.add(timeLabel).right();
            contentTable.add(infoTable).expandX().right();

            contentTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ScreenManager.setGameScreen(session);
                }
            });
        }

        this.add(contentTable).width(400).height(60).padRight(20);

        if (session != null) {
            TextButton clearBtn = new TextButton(Assets.getString("clear_save"), skin);
            this.add(clearBtn).width(150);

            clearBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Execute SQL DELETE statement
                    DatabaseManager.deleteSession(slotIndex);
                    clearSaveAndRefresh();
                }
            });
        } else {
            // Empty placeholder cell so the layout doesn't shift
            this.add().width(150);
        }
    }

    private void clearSaveAndRefresh() {
        // TODO: Trigger the file deletion logic here.
        this.session = null;
        buildCard();
    }
}
