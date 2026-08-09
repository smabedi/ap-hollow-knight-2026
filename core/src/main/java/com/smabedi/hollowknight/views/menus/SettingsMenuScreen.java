package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

/**
 * Provides the interactive UI for modifying persistent user configurations.
 * Handles bi-directional mapping between localized UI strings and internal storage keys,
 * and intercepts raw hardware inputs for dynamic control rebinding.
 */
public class SettingsMenuScreen extends MenuScreen {
    private String actionToBind = null;
    private TextButton buttonToUpdate = null;

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table mainColumns = new Table();
        root.add(mainColumns).expand().center().row();

        Table leftCol = new Table();
        leftCol.top().left();

        leftCol.add(new Label(Assets.getString("volume"), skin)).left();
        Slider volSlider = new Slider(0f, 1f, 0.05f, false, skin);
        volSlider.setValue(GameSettings.getMusicVolume());

        volSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.setMusicVolume(volSlider.getValue());
            }
        });
        leftCol.add(volSlider).width(200).padLeft(20).row();

        Table audioToggles = new Table();

        Label muteLabel = new Label(Assets.getString("audio"), skin);

        CheckBox sfxCheck = new CheckBox(" " + Assets.getString("sfx"), skin);
        sfxCheck.setChecked(GameSettings.shouldPlaySFX());

        sfxCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.setSfxMute(!sfxCheck.isChecked());
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        CheckBox musicCheck = new CheckBox(" " + Assets.getString("music"), skin);
        musicCheck.setChecked(GameSettings.shouldPlayMusic());

        musicCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.setMusicMute(!musicCheck.isChecked());
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        TextButton resetAudioBtn = new TextButton(Assets.getString("reset"), skin, "small");

        audioToggles.add(muteLabel).padRight(65);
        audioToggles.add(sfxCheck).padRight(65);
        audioToggles.add(musicCheck).padRight(65);
        audioToggles.add(resetAudioBtn).width(70);
        leftCol.add(audioToggles).left().colspan(2).padTop(20).padBottom(30).row();

        leftCol.add(new Label(Assets.getString("brightness"), skin)).left();
        Slider brightSlider = new Slider(0f, 2f, 0.1f, false, skin);
        brightSlider.setValue(GameSettings.getBrightness());

        brightSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.setBrightness(brightSlider.getValue());
            }
        });
        leftCol.add(brightSlider).width(200).padLeft(20).row();

        leftCol.add(new Label(Assets.getString("language"), skin)).left().padTop(30);
        SelectBox<String> langBox = new SelectBox<>(skin);
        langBox.setItems("English", "Français");
        langBox.setSelected(GameSettings.getLanguage().equals("fr") ? "Français" : "English");

        langBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedLang = langBox.getSelected();
                String newLangCode = selectedLang.equals("Français") ? "fr" : "en";

                if (!newLangCode.equals(GameSettings.getLanguage())) {
                    GameSettings.setLanguage(newLangCode);
                    Assets.getInstance().reloadLanguage();
                    ScreenManager.reloadLanguage();
                    ScreenManager.setMenuScreen(ScreenType.SETTINGS);
                }
            }
        });
        leftCol.add(langBox).width(150).padTop(30).padLeft(20).row();

        mainColumns.add(leftCol).padRight(50);

        leftCol.add(new Label(Assets.getString("menu_theme"), skin)).left().padTop(30).row();
        SelectBox<String> themeBox = new SelectBox<>(skin);

        // Inject dynamically localized strings to display familiar names to the user
        themeBox.setItems(
            Assets.getString("theme_void"),
            Assets.getString("theme_void_heart"),
            Assets.getString("theme_grimm_troupe"),
            Assets.getString("theme_eternal_ordeal")
        );

        // Fetch the raw configuration key and map it to its localized equivalent for the UI
        String currentThemeKey = GameSettings.getMenuTheme();
        themeBox.setSelected(Assets.getString(currentThemeKey));

        themeBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedDisplay = themeBox.getSelected();
                String newThemeKey = "theme_void";

                // Execute reverse-mapping: translate the localized UI string back into a system-safe storage key
                if (selectedDisplay.equals(Assets.getString("theme_void_heart"))) {
                    newThemeKey = "theme_void_heart";
                } else if (selectedDisplay.equals(Assets.getString("theme_grimm_troupe"))) {
                    newThemeKey = "theme_grimm_troupe";
                } else if (selectedDisplay.equals(Assets.getString("theme_eternal_ordeal"))) {
                    newThemeKey = "theme_eternal_ordeal";
                }

                if (!newThemeKey.equals(GameSettings.getMenuTheme())) {
                    GameSettings.setMenuTheme(newThemeKey);

                    // Re-initialize the rendering view to safely apply the new background texture
                    ScreenManager.reloadLanguage();
                    ScreenManager.setMenuScreen(ScreenType.SETTINGS);
                }
            }
        });

        leftCol.add(themeBox).width(200).padTop(10).padLeft(20).row();

        Table rightCol = new Table();
        rightCol.top().right();

        Table keysTable = new Table();
        keysTable.defaults().padBottom(10);

        addKeybindRow(keysTable, Assets.getString("up"), GameSettings.KEY_UP);
        addKeybindRow(keysTable, Assets.getString("down"), GameSettings.KEY_DOWN);
        addKeybindRow(keysTable, Assets.getString("right"), GameSettings.KEY_RIGHT);
        addKeybindRow(keysTable, Assets.getString("left"), GameSettings.KEY_LEFT);
        addKeybindRow(keysTable, Assets.getString("jump"), GameSettings.KEY_JUMP);
        addKeybindRow(keysTable, Assets.getString("attack"), GameSettings.KEY_ATTACK);
        addKeybindRow(keysTable, Assets.getString("dash"), GameSettings.KEY_DASH);
        addKeybindRow(keysTable, Assets.getString("focus_cast"), GameSettings.KEY_FOCUS);
        addKeybindRow(keysTable, Assets.getString("inventory"), GameSettings.KEY_INVENTORY);

        ScrollPane scrollPane = new ScrollPane(keysTable, skin);
        rightCol.add(scrollPane).height(200).width(450).row();

        TextButton resetKeysBtn = new TextButton(Assets.getString("reset_keyboard"), skin);
        rightCol.add(resetKeysBtn).padTop(20).fillX();

        mainColumns.add(rightCol).padLeft(50);

        Table bottomRow = new Table();
        TextButton backBtn = new TextButton(Assets.getString("back"), skin);

        bottomRow.add(backBtn).width(150);
        root.add(bottomRow).pad(100).bottom();

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (ScreenManager.isGameActive()) {
                    ScreenManager.resumeGame();
                } else {
                    ScreenManager.setMenuScreen(ScreenType.MAIN);
                }
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        resetAudioBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.resetAudio();
                volSlider.setValue(GameSettings.getMusicVolume());
                sfxCheck.setChecked(GameSettings.shouldPlaySFX());
                musicCheck.setChecked(GameSettings.shouldPlayMusic());
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        resetKeysBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.resetControls();
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });

        // Global Input Listener: Intercepts raw keycodes and redirects them to the configuration layer
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (buttonToUpdate != null) {
                    if (keycode == Input.Keys.ESCAPE) {
                        int currentKey = GameSettings.getKey(actionToBind);
                        buttonToUpdate.setText(Input.Keys.toString(currentKey));

                        cancelKeybind();
                        return true;
                    }

                    GameSettings.setKey(actionToBind, keycode);
                    buttonToUpdate.setText(Input.Keys.toString(keycode));
                    cancelKeybind();
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    private void addKeybindRow(Table table, String labelName, String actionKey) {
        table.add(new Label(labelName, skin)).left().expandX();

        int currentKeyCode = GameSettings.getKey(actionKey);
        TextButton keyBtn = new TextButton(Input.Keys.toString(currentKeyCode), skin, "small");
        table.add(keyBtn).width(100).right().row();

        keyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                actionToBind = actionKey;
                buttonToUpdate = keyBtn;
                keyBtn.setText(Assets.getString("press_key"));
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
    }

    private void cancelKeybind() {
        actionToBind = null;
        buttonToUpdate = null;
    }
}
