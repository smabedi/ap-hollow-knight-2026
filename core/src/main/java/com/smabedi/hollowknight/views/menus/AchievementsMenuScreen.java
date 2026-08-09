package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;
import com.smabedi.hollowknight.views.customelements.IconTextItem;

/**
 * Scene2D interface displaying the player's unlocked achievements.
 * Evaluates persistent GameSettings data to dynamically construct a scrollable status list.
 */
public class AchievementsMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label(Assets.getString("achievements"), skin);
        titleLabel.setColor(Color.GOLD);
        root.add(titleLabel).padTop(30).padBottom(20).row();

        Table content = new Table();
        content.top().left();
        content.defaults().pad(15).left();

        // Dynamically construct the achievement list based on persistent user configuration states.
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
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
        root.add(backBtn).width(150).padBottom(30);
    }

    private void addAchievementRow(Table table, String id, String title, String description) {
        boolean isUnlocked = GameSettings.isAchievementUnlocked(id);

        String regionName = isUnlocked ? "unlocked_mask" : "locked_mask";
        TextureRegion statusIcon = Assets.getUiAtlas().findRegion(regionName);

        // Instantiate a horizontal layout row (icon on the left, text on the right) for each achievement.
        IconTextItem achievementRow = new IconTextItem(statusIcon, title, description, skin, false, !isUnlocked);

        table.add(achievementRow).left().padBottom(20).row();
    }
}
