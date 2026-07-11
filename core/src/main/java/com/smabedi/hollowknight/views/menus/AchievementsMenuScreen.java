package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

public class AchievementsMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label(Assets.getString("achievements"), skin);
        root.add(titleLabel).padTop(30).padBottom(20).row();

        Table content = new Table();
        content.top().left();
        content.defaults().pad(15).left();

        // Build the list dynamically based on GameSettings Preferences
        addAchievementRow(content, "COMPLETION", Assets.getString("achv_COMPLETION"), Assets.getString("achv_desc_COMPLETION"));
        addAchievementRow(content, "SPEEDRUN", Assets.getString("achv_SPEEDRUN"), Assets.getString("achv_desc_SPEEDRUN"));
        addAchievementRow(content, "TRUE_HUNTER", Assets.getString("achv_TRUE_HUNTER"), Assets.getString("achv_desc_TRUE_HUNTER"));
        addAchievementRow(content, "DEFEAT_FALSE_KNIGHT", Assets.getString("achv_DEFEAT_FALSE_KNIGHT"), Assets.getString("achv_desc_DEFEAT_FALSE_KNIGHT"));
        addAchievementRow(content, "ONE_SHOT", Assets.getString("achv_ONE_SHOT"), Assets.getString("achv_desc_ONE_SHOT"));

        ScrollPane scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);
        root.add(scrollPane).expand().fill().pad(20).row();

        TextButton backBtn = new TextButton(Assets.getString("back"), skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });
        root.add(backBtn).width(150).padBottom(30);
    }

    private void addAchievementRow(Table table, String id, String title, String description) {
        boolean isUnlocked = GameSettings.isAchievementUnlocked(id);

        Table row = new Table();
        Table textTable = new Table();

        Label titleLbl = new Label(title, skin);
        Label descLbl = new Label(description, skin);

        // Apply Grayscale/Faded look if locked
        if (!isUnlocked) {
            titleLbl.setColor(Color.GRAY);
            descLbl.setColor(Color.DARK_GRAY);
            titleLbl.setText(title + " (" + Assets.getString("locked") + ")");
        } else {
            titleLbl.setColor(Color.GOLD);
        }

        descLbl.setWrap(true);
        textTable.add(titleLbl).left().row();
        textTable.add(descLbl).width(600).left().row();

        row.add(textTable).expandX().left();
        table.add(row).row();
    }
}
