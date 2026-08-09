package com.smabedi.hollowknight.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.repositories.DatabaseManager;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.game.LocationType;

/**
 * A custom Scene2D Table widget representing an interactable save slot.
 * Handles the display of session data (playtime, location, health) and delegates
 * load/delete operations to the DatabaseManager.
 */
public class SaveCard extends Table {
    private final int slotIndex;
    private final Skin skin;
    private GameSession session;
    private SaveCardHudActor hudActor;

    public SaveCard(int slotIndex, GameSession session, Skin skin) {
        this.slotIndex = slotIndex;
        this.session = session;
        this.skin = skin;

        buildCard();
    }

    /**
     * Constructs the visual layout of the save card. Dynamically alters its appearance
     * and functionality based on whether the assigned slot contains existing session data.
     */
    private void buildCard() {
        this.clearChildren();
        this.left().center();

        Label slotLabel = new Label(slotIndex + ".", skin);
        this.add(slotLabel).width(30);

        Button contentTable = new Button(skin);
        contentTable.clearChildren();
        contentTable.left();

        if (session == null) {
            Button.ButtonStyle emptyStyle = new Button.ButtonStyle(skin.get(Button.ButtonStyle.class));
            emptyStyle.up = new TextureRegionDrawable(Assets.getUiAtlas().findRegion("area_abyss"));
            contentTable.setStyle(emptyStyle);
            Label newGameLabel = new Label(Assets.getString("new_game"), skin);
            contentTable.add(newGameLabel).left().expandX();

            contentTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSession newSession = new GameSession(slotIndex);
                    DatabaseManager.saveSession(newSession);
                    ScreenManager.setGameScreen(newSession);
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
                }
            });
        } else {
            String areaRegion = session.location == LocationType.GREENPATH ? "area_greenpath" : "area_crossroads";
            Button.ButtonStyle filledStyle = new Button.ButtonStyle(skin.get(Button.ButtonStyle.class));
            filledStyle.up = new TextureRegionDrawable(Assets.getUiAtlas().findRegion(areaRegion));
            contentTable.setStyle(filledStyle);

            hudActor = new SaveCardHudActor(session.health, session.soul);
            contentTable.add(hudActor).size(hudActor.getWidth(), hudActor.getHeight()).padRight(50);

            Label locationLabel = new Label(session.location.getName(), skin);
            Label timeLabel = new Label(session.playtime / 60 + " " + Assets.getString("min"), skin);

            Table infoTable = new Table();
            infoTable.add(locationLabel).left().row();
            infoTable.add(timeLabel).left();
            contentTable.add(infoTable).expandX().left().padLeft(30);

            contentTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ScreenManager.setGameScreen(session);
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
                }
            });
        }

        this.add(contentTable).width(750).expandY().fillY().padRight(20);

        if (session != null) {
            TextButton clearBtn = new TextButton(Assets.getString("clear_save"), skin, "small");
            this.add(clearBtn).width(150);

            clearBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    DatabaseManager.deleteSession(slotIndex);
                    clearSaveAndRefresh();
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
                }
            });
        } else {
            this.add().width(150);
        }
    }

    /**
     * Safely purges the UI elements and visual trackers of a deleted save,
     * triggering a structural rebuild of the card widget into an empty state.
     */
    private void clearSaveAndRefresh() {
        if (hudActor != null) {
            hudActor.dispose();
            hudActor = null;
        }
        this.session = null;
        buildCard();
    }
}
