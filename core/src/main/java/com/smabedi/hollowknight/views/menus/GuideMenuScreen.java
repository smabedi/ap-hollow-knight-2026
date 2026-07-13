package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

public class GuideMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label titleLabel = new Label(Assets.getString("guide"), skin);
        // Assuming your skin has a larger font style, you can set it here if desired
        root.add(titleLabel).padTop(30).padBottom(20).row();

        // Content Table (Goes inside ScrollPane)
        Table content = new Table();
        content.top().left();
        content.defaults().pad(10).left();

        // --- SECTION 1: DYNAMIC CONTROLS ---
        Label controlsTitle = new Label("--- " + Assets.getString("controls") + " ---", skin);
        controlsTitle.setColor(com.badlogic.gdx.graphics.Color.GOLD);
        content.add(controlsTitle).padTop(20).row();

        addControlRow(content, Assets.getString("move"), GameSettings.KEY_LEFT, GameSettings.KEY_RIGHT);
        addControlRow(content, Assets.getString("up_and_down"), GameSettings.KEY_UP, GameSettings.KEY_DOWN);
        addControlRow(content, Assets.getString("jump"), GameSettings.KEY_JUMP);
        addControlRow(content, Assets.getString("attack"), GameSettings.KEY_ATTACK);
        addControlRow(content, Assets.getString("dash"), GameSettings.KEY_DASH);
        addControlRow(content, Assets.getString("focus_cast"), GameSettings.KEY_FOCUS);

        // --- SECTION 2: MECHANICS & ABILITIES ---
        Label mechanicsTitle = new Label("--- " + Assets.getString("mechanics") + " ---", skin);
        mechanicsTitle.setColor(com.badlogic.gdx.graphics.Color.GOLD);
        content.add(mechanicsTitle).padTop(30).row();

        addDescription(content, Assets.getString("guide_health_desc"));
        addDescription(content, Assets.getString("guide_soul_desc"));
        addDescription(content, Assets.getString("guide_spells_desc"));

        // --- SECTION 3: CHEAT CODES ---
        Label cheatsTitle = new Label("--- " + Assets.getString("cheat_codes") + " ---", skin);
        cheatsTitle.setColor(com.badlogic.gdx.graphics.Color.GOLD);
        content.add(cheatsTitle).padTop(30).row();

        addDescription(content, Assets.getString("guide_cheat_boss") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.BOSS_TELEPORT));
        addDescription(content, Assets.getString("guide_cheat_noclip") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.SPECTATOR_MODE));
        addDescription(content, Assets.getString("guide_cheat_heal") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.EMERGENCY_HEAL));
        addDescription(content, Assets.getString("guide_cheat_soul") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.REFILL_SOUL));
        addDescription(content, Assets.getString("guide_cheat_god") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.GOD_MODE));
        addDescription(content, Assets.getString("guide_cheat_dilation") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.TIME_DILATION));

        // Wrap content in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);
        root.add(scrollPane).expand().fill().pad(20).row();

        // Back Button
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

    private void addControlRow(Table table, String actionName, String... settingKeys) {
        StringBuilder keysStr = new StringBuilder();
        for (int i = 0; i < settingKeys.length; i++) {
            keysStr.append(Input.Keys.toString(GameSettings.getKey(settingKeys[i])));
            if (i < settingKeys.length - 1) keysStr.append(" / ");
        }
        Label lbl = new Label(actionName + ": " + keysStr, skin);
        table.add(lbl).row();
    }

    private void addDescription(Table table, String text) {
        Label lbl = new Label(text, skin);
        lbl.setWrap(true);
        lbl.setAlignment(Align.left);
        // Width limits the text so it wraps instead of pushing the table endlessly wide
        table.add(lbl).width(700).padBottom(10).row();
    }
}
