package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenType;
import com.smabedi.hollowknight.views.ScreenManager;

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
        CheckBox sfxCheck = new CheckBox(Assets.getString("sfx"), skin);
        sfxCheck.setChecked(GameSettings.shouldPlaySFX());

        sfxCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.setSfxMute(!sfxCheck.isChecked());
            }
        });

        CheckBox musicCheck = new CheckBox(Assets.getString("music"), skin);
        musicCheck.setChecked(GameSettings.shouldPlayMusic());

        musicCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.setMusicMute(!musicCheck.isChecked());
            }
        });

        TextButton resetAudioBtn = new TextButton(Assets.getString("reset"), skin);

        audioToggles.add(sfxCheck).padRight(15);
        audioToggles.add(musicCheck).padRight(15);
        audioToggles.add(resetAudioBtn);
        leftCol.add(audioToggles).colspan(2).padTop(20).padBottom(30).row();

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
        rightCol.add(scrollPane).height(200).width(300).row();

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
            }
        });

        resetAudioBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.resetAudio();
                volSlider.setValue(GameSettings.getMusicVolume());
                sfxCheck.setChecked(GameSettings.shouldPlaySFX());
                musicCheck.setChecked(GameSettings.shouldPlayMusic());
            }
        });

        resetKeysBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.resetControls();
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
            }
        });

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
        TextButton keyBtn = new TextButton(Input.Keys.toString(currentKeyCode), skin);
        table.add(keyBtn).width(100).right().row();

        keyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                actionToBind = actionKey;
                buttonToUpdate = keyBtn;
                keyBtn.setText(Assets.getString("press_key"));
            }
        });
    }

    private void cancelKeybind() {
        actionToBind = null;
        buttonToUpdate = null;
    }
}
