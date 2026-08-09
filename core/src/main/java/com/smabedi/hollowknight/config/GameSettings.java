package com.smabedi.hollowknight.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.smabedi.hollowknight.models.entities.enemies.EnemyType;

/**
 * Handles persistent user configurations (Keybinds, Audio, Achievements)
 * backed by LibGDX Preferences. Data is maintained between game sessions.
 */
public class GameSettings {
    // Setting Keys
    public static final String MUSIC_VOL = "music_vol";
    public static final String MUSIC_MUTE = "music_mute";
    public static final String SFX_MUTE = "sfx_mute";
    public static final String BRIGHTNESS = "brightness";
    public static final String LANGUAGE = "language"; // "en" for English and "fr" for French
    public static final String MENU_THEME = "menu_theme";

    // Keybind Keys
    public static final String KEY_LEFT = "key_left";
    public static final String KEY_RIGHT = "key_right";
    public static final String KEY_UP = "key_up";
    public static final String KEY_DOWN = "key_down";
    public static final String KEY_JUMP = "key_jump";
    public static final String KEY_ATTACK = "key_attack";
    public static final String KEY_DASH = "key_dash";
    public static final String KEY_FOCUS = "key_focus";
    public static final String KEY_INVENTORY = "key_inventory";

    private static final String PREF_NAME = "hollow_knight_settings";
    private static Preferences prefs;

    /**
     * Initializes preferences on startup. If data is absent (first launch),
     * default configurations are established and flushed to disk.
     */
    public static void load() {
        prefs = Gdx.app.getPreferences(PREF_NAME);

        if (!prefs.contains(MUSIC_VOL)) {
            resetAudio();
            resetControls();
            setBrightness(1f);
            setLanguage("en");
            setMenuTheme("theme_void");

            // Setup tracking for the 'True Hunter' achievement
            for (EnemyType type : EnemyType.values()) {
                prefs.putBoolean("killed_" + type.name(), false);
            }
            prefs.flush();
        }
    }

    public static void resetAudio() {
        prefs.putFloat(MUSIC_VOL, 0.8f); // 80% Volume
        prefs.putBoolean(MUSIC_MUTE, false);
        prefs.putBoolean(SFX_MUTE, false);
        prefs.flush();
    }

    public static float getMusicVolume() {
        return prefs.getFloat(MUSIC_VOL);
    }

    public static void setMusicVolume(float vol) {
        prefs.putFloat(MUSIC_VOL, vol);
        prefs.flush();
    }

    public static boolean shouldPlayMusic() {
        return !prefs.getBoolean(MUSIC_MUTE);
    }

    public static boolean shouldPlaySFX() {
        return !prefs.getBoolean(SFX_MUTE);
    }

    public static void setMusicMute(boolean mute) {
        prefs.putBoolean(MUSIC_MUTE, mute);
        prefs.flush();
    }

    public static void setSfxMute(boolean mute) {
        prefs.putBoolean(SFX_MUTE, mute);
        prefs.flush();
    }

    public static void resetControls() {
        prefs.putInteger(KEY_LEFT, Input.Keys.LEFT);
        prefs.putInteger(KEY_RIGHT, Input.Keys.RIGHT);
        prefs.putInteger(KEY_UP, Input.Keys.UP);
        prefs.putInteger(KEY_DOWN, Input.Keys.DOWN);
        prefs.putInteger(KEY_JUMP, Input.Keys.Z);
        prefs.putInteger(KEY_ATTACK, Input.Keys.X);
        prefs.putInteger(KEY_DASH, Input.Keys.C);
        prefs.putInteger(KEY_FOCUS, Input.Keys.A);
        prefs.putInteger(KEY_INVENTORY, Input.Keys.I);
        prefs.flush();
    }

    public static int getKey(String action) {
        return prefs.getInteger(action);
    }

    public static void setKey(String action, int keycode) {
        prefs.putInteger(action, keycode);
        prefs.flush();
    }

    public static float getBrightness() {
        return prefs.getFloat(BRIGHTNESS);
    }

    public static void setBrightness(float b) {
        prefs.putFloat(BRIGHTNESS, b);
        prefs.flush();
    }

    public static String getLanguage() {
        return prefs.getString(LANGUAGE);
    }

    public static void setLanguage(String lang) {
        prefs.putString(LANGUAGE, lang);
        prefs.flush();
    }

    public static boolean isAchievementUnlocked(String achievementId) {
        return prefs.getBoolean("achv_" + achievementId, false);
    }

    public static void unlockAchievement(String achievementId) {
        prefs.putBoolean("achv_" + achievementId, true);
        prefs.flush();
    }

    public static String getMenuTheme() {
        return prefs.getString(MENU_THEME, "theme_void");
    }

    public static void setMenuTheme(String theme) {
        prefs.putString(MENU_THEME, theme);
        prefs.flush();
    }

    public static void registerEnemyKill(String enemyName) {
        prefs.putBoolean("killed_" + enemyName, true);
        prefs.flush();
    }

    public static boolean hasKilledEnemy(String enemyName) {
        return prefs.getBoolean("killed_" + enemyName, false);
    }
}
