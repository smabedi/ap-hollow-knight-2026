package com.smabedi.hollowknight.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

public class Assets {
    private static Assets instance;
    public final AssetManager manager;
    public I18NBundle bundle;

    private Assets() {
        manager = new AssetManager();
        bundle = I18NBundle.createBundle(
            Gdx.files.internal(Constants.Paths.STRINGS),
            Locale.of(GameSettings.getLanguage())
        );
    }

    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }

    private static AssetManager getManager() {
        return getInstance().manager;
    }

    private static I18NBundle getBundle() {
        return getInstance().bundle;
    }

    public void reloadLanguage() {
        bundle = I18NBundle.createBundle(
            Gdx.files.internal(Constants.Paths.STRINGS),
            Locale.of(GameSettings.getLanguage())
        );
    }

    public static void loadAssets() {
        getManager().load(Constants.Paths.SKIN, Skin.class);
        getManager().finishLoading();
    }

    public static void dispose() {
        getManager().dispose();
    }

    public static Skin getSkin() {
        return getManager().get(Constants.Paths.SKIN, Skin.class);
    }

    public static String getString(String key) {
        return getBundle().get(key);
    }
}
