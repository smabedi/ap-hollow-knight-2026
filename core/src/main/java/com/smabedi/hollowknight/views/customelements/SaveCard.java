package com.smabedi.hollowknight.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.models.game.GameSession;
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
        this.add(slotLabel).padRight(10).width(30);

        Button contentTable = new Button(skin);
        contentTable.left();

        if (session == null) {
            Label newGameLabel = new Label(Assets.getString("new_game"), skin);
            contentTable.add(newGameLabel).left().expandX();

            contentTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSession newSession = new GameSession();
                    // TODO: Add the SQL save logic here later.
                    ScreenManager.setGameScreen(newSession);
                }
            });
        } else {
            Label masksLabel = new Label(Assets.getString("mask") + ": " + session.getHealth(), skin);
            Label geoLabel = new Label(Assets.getString("soul") + ": " + session.getSoul(), skin);
            Label locationLabel = new Label(session.getLocation().getName(), skin);
            Label timeLabel = new Label(session.getPlaytime() + " " + Assets.getString("min"), skin);

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

        this.add(contentTable).width(500).height(80).padRight(20);

        if (session != null) {
            TextButton clearBtn = new TextButton(Assets.getString("clear_save"), skin);
            this.add(clearBtn).width(150);

            clearBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    System.out.println("Clearing save on slot: " + slotIndex);
                    // TODO: Delete the JSON file for this slot
                    // Refresh the UI to show "NEW GAME"
                    clearSaveAndRefresh();
                }
            });
        } else {
            // Empty placeholder cell so the layout doesn't shift
            this.add().width(150);
        }
    }

    private void clearSaveAndRefresh() {
        // Trigger the file deletion logic.
        this.session = null; // (Make session non-final if you want to mutate it directly)
        buildCard();
    }
}
