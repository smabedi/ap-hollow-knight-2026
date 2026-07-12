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
        getManager().load(Constants.Paths.Textures.KNIGHT_ATLAS, TextureAtlas.class);
        getManager().load(Constants.Paths.Textures.VFX_ATLAS, TextureAtlas.class);
        getManager().load(Constants.Paths.Textures.ENTITY_ATLAS, TextureAtlas.class);
        getManager().load(Constants.Paths.Textures.BOSS_ATLAS, TextureAtlas.class);
        getManager().load(Constants.Paths.Textures.HUD_ATLAS, TextureAtlas.class);
        getManager().load(Constants.Paths.Videos.BACKGROUND, TextureAtlas.class);
        getManager().load(Constants.Paths.Sounds.BGM_MENU, Music.class);
        getManager().load(Constants.Paths.Sounds.BGM_CROSSROADS, Music.class);
        getManager().load(Constants.Paths.Sounds.BGM_GREENPATH, Music.class);
        getManager().load(Constants.Paths.Sounds.BGM_BOSS, Music.class);
        getManager().load(Constants.Paths.Sounds.BGM_WIN, Music.class);
        getManager().load(Constants.Paths.Sounds.BOSS_TRANSITION, Sound.class);
//        getManager().load(Constants.Paths.Sounds.SFX_NAIL_SLASH, Sound.class);
//        getManager().load(Constants.Paths.Sounds.SFX_DASH, Sound.class);
//        getManager().load(Constants.Paths.Sounds.SFX_DAMAGE, Sound.class);
//        getManager().load(Constants.Paths.Sounds.SFX_FOCUS, Sound.class);
//        getManager().load(Constants.Paths.Sounds.SFX_SPELL_CAST, Sound.class);
//        getManager().load(Constants.Paths.Sounds.SFX_ZOTE, Sound.class);
        getManager().finishLoading();
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

    public static TextureAtlas getHudAtlas() {
        return getManager().get(Constants.Paths.Textures.HUD_ATLAS, TextureAtlas.class);
    }

    public static TextureAtlas getBackgroundAtlas() {
        return getManager().get(Constants.Paths.Videos.BACKGROUND, TextureAtlas.class);
    }

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
}
