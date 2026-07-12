package com.smabedi.hollowknight.config;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class AudioManager {
    private static AudioManager instance;
    private Music currentMusic;
    private Music nextMusic;
    private float fadeTimer = 0f;

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Updates crossfades. Hook this directly into your main GameScreen loop.
     */
    public void update(float dt) {
        float maxMusicVolume = GameSettings.getMusicVolume();

        if (nextMusic != null) {
            fadeTimer += dt;
            float fadeDuration = 1.5f;
            float progress = MathUtils.clamp(fadeTimer / fadeDuration, 0f, 1f);

            // Crossfade math
            if (currentMusic != null && GameSettings.shouldPlayMusic()) {
                currentMusic.setVolume((1f - progress) * maxMusicVolume);
            }
            if (GameSettings.shouldPlayMusic()) {
                nextMusic.setVolume(progress * maxMusicVolume);
            }

            if (progress >= 1f) {
                if (currentMusic != null) currentMusic.stop();
                currentMusic = nextMusic;
                nextMusic = null;
            }
        } else if (currentMusic != null && currentMusic.isPlaying()) {
            // Live volume sync with slider movements
            if (!GameSettings.shouldPlayMusic()) {
                currentMusic.setVolume(0f);
            } else {
                currentMusic.setVolume(maxMusicVolume);
            }
        }
    }

    private void playMusicCore(String internalPath, boolean loop) {
        if (!Assets.getInstance().manager.isLoaded(internalPath, Music.class)) {
            System.err.println("Music asset not loaded yet: " + internalPath);
            return;
        }

        Music target = Assets.getInstance().manager.get(internalPath, Music.class);
        if (currentMusic == target || nextMusic == target) return;

        target.setLooping(loop);

        if (currentMusic == null) {
            currentMusic = target;
            if (GameSettings.shouldPlayMusic()) {
                currentMusic.setVolume(GameSettings.getMusicVolume());
                currentMusic.play();
            }
        } else {
            nextMusic = target;
            fadeTimer = 0f;
            if (GameSettings.shouldPlayMusic()) {
                nextMusic.setVolume(0f);
                nextMusic.play();
            }
        }
    }

    /**
     * One-liner music manager featuring asset validation and automated crossfades.
     */
    public static void playMusic(String internalPath, boolean loop) {
        getInstance().playMusicCore(internalPath, loop);
    }

    private long playSfxCore(String internalPath) {
        return playSfx(internalPath, 1f, 1f, 0f);
    }

    /**
     * Fires a simple, non-spatialized global sound effect.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static long playSfx(String internalPath) {
        return getInstance().playSfxCore(internalPath);
    }

    private long playSfxVariedCore(String internalPath, float minPitch, float maxPitch) {
        return playSfx(internalPath, 1f, MathUtils.random(minPitch, maxPitch), 0f);
    }

    /**
     * Specialized overload for randomized pitch variations (e.g., weapon swings, grunts).
     */
    public static long playSfxVaried(String internalPath, float minPitch, float maxPitch) {
        return getInstance().playSfxVariedCore(internalPath, minPitch, maxPitch);
    }

    private long playSpatialSfxCore(String internalPath, Vector2 sourcePos, Vector2 listenerPos, float falloffRadius) {
        float distance = sourcePos.dst(listenerPos);
        if (distance > falloffRadius) return -1;

        // Linear attenuation formula
        float volume = 1f - (distance / falloffRadius);

        // Panning map: negative value left side, positive value right side
        float pan = MathUtils.clamp((sourcePos.x - listenerPos.x) / falloffRadius, -1f, 1f);

        return playSfx(internalPath, volume, 1f, pan);
    }

    /**
     * Contextual sound tracking tied directly to normalized Box2D world coordinate vectors.
     */
    public static long playSpatialSfx(String internalPath, Vector2 sourcePos, Vector2 listenerPos, float falloffRadius) {
        return getInstance().playSpatialSfxCore(internalPath, sourcePos, listenerPos, falloffRadius);
    }

    private long playSfx(String internalPath, float baseVolume, float pitch, float pan) {
        if (!GameSettings.shouldPlaySFX()) return -1;

        if (!Assets.getInstance().manager.isLoaded(internalPath, Sound.class)) {
            System.err.println("Sound asset not loaded yet: " + internalPath);
            return -1;
        }

        Sound sound = Assets.getInstance().manager.get(internalPath, Sound.class);
        return sound.play(baseVolume, pitch, pan);
    }

    public void stopCurrentMusic() {
        if (currentMusic != null) currentMusic.stop();
        if (nextMusic != null) nextMusic.stop();
        currentMusic = null;
        nextMusic = null;
    }
}
