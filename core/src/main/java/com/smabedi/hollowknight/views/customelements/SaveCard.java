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

            // Inject the custom scaled replica of the main game HUD
            hudActor = new SaveCardHudActor(session.health, session.soul);
            contentTable.add(hudActor).size(hudActor.getWidth(), hudActor.getHeight()).padRight(50);

            Label locationLabel = new Label(session.location.getName(), skin);
            Label timeLabel = new Label(session.playtime / 60 + " " + Assets.getString("min"), skin);

            Table infoTable = new Table();
            // Change .right() to .left() so text reads cleanly outwards from the HUD actor space
            infoTable.add(locationLabel).right().row();
            infoTable.add(timeLabel).right();
            // Change .right() to .expandX().left() so it grabs all remaining space and sits cleanly next to the masks
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

    private void clearSaveAndRefresh() {
        if (hudActor != null) {
            hudActor.dispose();
            hudActor = null;
        }
        this.session = null;
        buildCard();
    }
}
