package com.smabedi.hollowknight.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.ray3k.stripe.FreeTypeSkinLoader;

import java.util.Locale;

/**
 * Singleton wrapper around LibGDX's AssetManager.
 * Centralizes the loading, tracking, and retrieval of all external resources
 * (Textures, Audio, UI Skins, Localization Strings) to prevent memory leaks
 * and ensure synchronous loading during the splash screen.
 */
public class Assets {
    private static Assets instance;
    public final AssetManager manager;

    // Handles multi-language string extraction based on GameSettings
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

    public static <T> void load(String fileName, Class<T> type) {
        getManager().load(fileName, type);
    }

    /**
     * Queues all necessary game assets into the AssetManager.
     * This must be called before the loading screen begins updating the manager.
     */
    public static void queueAssets() {
        // Custom loader required to parse TTF fonts embedded within the UI Skin JSON
        getManager().setLoader(Skin.class, new FreeTypeSkinLoader(getManager().getFileHandleResolver()));

        load(Constants.Paths.SKIN, Skin.class);
        load(Constants.Paths.Textures.KNIGHT_ATLAS, TextureAtlas.class);
        load(Constants.Paths.Textures.VFX_ATLAS, TextureAtlas.class);
        load(Constants.Paths.Textures.ENTITY_ATLAS, TextureAtlas.class);
        load(Constants.Paths.Textures.BOSS_ATLAS, TextureAtlas.class);
        load(Constants.Paths.Textures.UI_ATLAS, TextureAtlas.class);
        load(Constants.Paths.Videos.BACKGROUND, TextureAtlas.class);
        load(Constants.Paths.Sounds.BGM_MENU, Music.class);
        load(Constants.Paths.Sounds.BGM_CROSSROADS, Music.class);
        load(Constants.Paths.Sounds.BGM_GREENPATH, Music.class);
        load(Constants.Paths.Sounds.BGM_BOSS, Music.class);
        load(Constants.Paths.Sounds.BGM_WIN, Music.class);
        load(Constants.Paths.Sounds.SFX_BOSS_TRANSITION, Sound.class);
        load(Constants.Paths.Sounds.SFX_FOCUS, Sound.class);
        load(Constants.Paths.Sounds.SFX_DAMAGE, Sound.class);
        load(Constants.Paths.Sounds.SFX_DASH, Sound.class);
        load(Constants.Paths.Sounds.SFX_JUMP, Sound.class);
        load(Constants.Paths.Sounds.SFX_RUN, Sound.class);
        load(Constants.Paths.Sounds.SFX_LAND, Sound.class);
        load(Constants.Paths.Sounds.SFX_SPELL_CAST, Sound.class);
        load(Constants.Paths.Sounds.SFX_VOID_SPELL_CAST, Sound.class);
        load(Constants.Paths.Sounds.SFX_WALL_SLIDE, Sound.class);
        load(Constants.Paths.Sounds.SFX_NOTIFICATION, Sound.class);
        load(Constants.Paths.Sounds.SFX_ACHIEVEMENT, Sound.class);
        load(Constants.Paths.Sounds.SFX_ZOTE_ATTACK, Sound.class);
        load(Constants.Paths.Sounds.SFX_UI_BUTTON, Sound.class);
        load(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, Sound.class);
        load(Constants.Paths.Sounds.SFX_ENEMY_WALKING, Sound.class);
        load(Constants.Paths.Sounds.SFX_GUARDIAN_RUNNING_LOOP, Sound.class);
        load(Constants.Paths.Sounds.SFX_LASER_BURST, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_STUN_HIT, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_ARMOR_HIT, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_OPEN_ARMOR_HIT, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_SWING, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_STRIKE, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_POWER_STRIKE, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_JUMP, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_LAND, Sound.class);
        load(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, Sound.class);
        load(Constants.Paths.Sounds.SFX_SOUL_FULL, Sound.class);

        for (String path : Constants.Paths.Sounds.SFX_FK_ROAR) {
            load(path, Sound.class);
        }
        for (String path : Constants.Paths.Sounds.SFX_SOUL_PICKUP) {
            load(path, Sound.class);
        }
        for (String path : Constants.Paths.Sounds.SFX_ZOTE) {
            load(path, Sound.class);
        }
        for (String path : Constants.Paths.Sounds.SFX_SLASH) {
            load(path, Sound.class);
        }
    }

    public static Skin getSkin() {
        return getManager().get(Constants.Paths.SKIN, Skin.class);
    }

    public static TextureAtlas getKnightAtlas() {
        return getManager().get(Constants.Paths.Textures.KNIGHT_ATLAS, TextureAtlas.class);
    }

    public static TextureAtlas getVfxAtlas() {
        return getManager().get(Constants.Paths.Textures.VFX_ATLAS, TextureAtlas.class);
    }

    public static TextureAtlas getEntityAtlas() {
        return getManager().get(Constants.Paths.Textures.ENTITY_ATLAS, TextureAtlas.class);
    }

    public static TextureAtlas getBossAtlas() {
        return getManager().get(Constants.Paths.Textures.BOSS_ATLAS, TextureAtlas.class);
    }

    public static TextureAtlas getUiAtlas() {
        return getManager().get(Constants.Paths.Textures.UI_ATLAS, TextureAtlas.class);
    }

    public static TextureAtlas getBackgroundAtlas() {
        return getManager().get(Constants.Paths.Videos.BACKGROUND, TextureAtlas.class);
    }

    // --- Pre-configured Animation Getters ---

    public static Animation<TextureRegion> getShockwaveVfx() {
        return new Animation<>(0.2f, getVfxAtlas().findRegions("shockwave"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getNormalDashVfx() {
        return new Animation<>(0.05f, getKnightAtlas().findRegions("knight_dash_burst"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getShadowDashVfx() {
        return new Animation<>(0.05f, getKnightAtlas().findRegions("knight_shadow_dash_trail"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getWraithsVfx() {
        return new Animation<>(0.1f, getVfxAtlas().findRegions("scream"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getVoidWraithsVfx() {
        return new Animation<>(0.1f, getVfxAtlas().findRegions("shadow_scream"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getSpiritCastVfx() {
        return new Animation<>(0.05f, getVfxAtlas().findRegions("spirit_wave"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getVoidSpiritCastVfx() {
        return new Animation<>(0.05f, getVfxAtlas().findRegions("spirit_wave"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getSpiritProjectile() {
        return new Animation<>(0.1f, getVfxAtlas().findRegions("spirit_ball"), Animation.PlayMode.NORMAL);
    }

    public static Animation<TextureRegion> getVoidSpiritProjectile() {
        return new Animation<>(0.1f, getVfxAtlas().findRegions("shadow_spirit"), Animation.PlayMode.NORMAL);
    }

    public static String getString(String key) {
        return getBundle().get(key);
    }

    public static void dispose() {
        getManager().dispose();
    }

    /**
     * Rebuilds the I18NBundle. Called dynamically when the user changes the language setting.
     */
    public void reloadLanguage() {
        bundle = I18NBundle.createBundle(
            Gdx.files.internal(Constants.Paths.STRINGS),
            Locale.of(GameSettings.getLanguage())
        );
    }
}
