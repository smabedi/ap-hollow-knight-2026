package com.smabedi.hollowknight.config;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;


public class AudioManager {
    private static AudioManager instance;
    private Music currentMusic;
    private Music nextMusic;
    private float fadeTimer = 0f;
    private static final Random random = new Random();

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

    /**
     * Overloaded playSfx method that automatically selects a random
     * variant from an array of file paths.
     */
    public static void playSfx(String[] paths) {
        if (paths == null || paths.length == 0) return;
        int randomIndex = random.nextInt(paths.length);
        playSfx(paths[randomIndex]);
    }

    /**
     * Starts a standard, non-spatial looping sound effect (ideal for the player character).
     * Returns the unique instance ID required to stop the loop.
     */
    public static long loopSfx(String internalPath) {
        if (!GameSettings.shouldPlaySFX()) return -1;
        if (!Assets.getInstance().manager.isLoaded(internalPath, Sound.class)) return -1;

        Sound sound = Assets.getInstance().manager.get(internalPath, Sound.class);
        // 1f volume, 1f pitch, 0f pan (dead center)
        return sound.loop(1f, 1f, 0f);
    }

    private long playSfxVariedCore(String internalPath, float minPitch, float maxPitch) {
        return playSfx(internalPath, 1f, MathUtils.random(minPitch, maxPitch), 0f);
    }

    /**
     * Specialized overload for randomized pitch variations (e.g., weapon swings, grunts).
     */
    @SuppressWarnings("UnusedReturnValue")
    public static long playSfxVaried(String internalPath, float minPitch, float maxPitch) {
        return getInstance().playSfxVariedCore(internalPath, minPitch, maxPitch);
    }

    /**
     * Overloaded playSfxVaried method that selects a random file variant
     * AND applies pitch shifting to eliminate acoustic fatigue completely!
     */
    public static void playSfxVaried(String[] paths, float minPitch, float maxPitch) {
        if (paths == null || paths.length == 0) return;
        int randomIndex = random.nextInt(paths.length);
        playSfxVaried(paths[randomIndex], minPitch, maxPitch);
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
    @SuppressWarnings("UnusedReturnValue")
    public static long playSpatialSfx(String internalPath, Vector2 sourcePos, Vector2 listenerPos, float falloffRadius) {
        return getInstance().playSpatialSfxCore(internalPath, sourcePos, listenerPos, falloffRadius);
    }

    // Inside config/AudioManager.java

    /**
     * Starts a looping sound effect with spatial panning and volume.
     * Returns the unique instance ID required to update or stop the loop.
     */
    public static long loopSpatialSfx(String internalPath, Vector2 sourcePos, Vector2 listenerPos, float falloffRadius) {
        if (!GameSettings.shouldPlaySFX()) return -1;
        if (!Assets.getInstance().manager.isLoaded(internalPath, Sound.class)) return -1;

        float distance = sourcePos.dst(listenerPos);
        float volume = distance > falloffRadius ? 0f : 1f - (distance / falloffRadius);
        float pan = MathUtils.clamp((sourcePos.x - listenerPos.x) / falloffRadius, -1f, 1f);

        Sound sound = Assets.getInstance().manager.get(internalPath, Sound.class);
        return sound.loop(volume, 1f, pan);
    }

    /**
     * Updates the volume and 3D pan of an already looping sound instance based on live coordinates.
     */
    public static void updateSpatialSfx(String internalPath, long soundId, Vector2 sourcePos, Vector2 listenerPos, float falloffRadius) {
        if (soundId == -1 || !GameSettings.shouldPlaySFX()) return;
        if (!Assets.getInstance().manager.isLoaded(internalPath, Sound.class)) return;

        float distance = sourcePos.dst(listenerPos);
        float volume = distance > falloffRadius ? 0f : 1f - (distance / falloffRadius);
        float pan = MathUtils.clamp((sourcePos.x - listenerPos.x) / falloffRadius, -1f, 1f);

        Sound sound = Assets.getInstance().manager.get(internalPath, Sound.class);
        sound.setPan(soundId, pan, volume);
    }

    /**
     * Safely terminates a specific looped sound instance.
     */
    public static void stopSfx(String internalPath, long soundId) {
        if (soundId == -1) return;
        if (!Assets.getInstance().manager.isLoaded(internalPath, Sound.class)) return;

        Sound sound = Assets.getInstance().manager.get(internalPath, Sound.class);
        sound.stop(soundId);
    }

    /**
     * Instantly kills all looping and playing SFX instances globally.
     * Call this when exiting a game session to return to the main menu.
     */
    public static void stopAllSfx() {
        if (Assets.getInstance().manager == null) return;

        Array<Sound> allSounds = new Array<>();
        Assets.getInstance().manager.getAll(Sound.class, allSounds);

        //noinspection GDXJavaUnsafeIterator
        for (Sound sound : allSounds) {
            sound.stop(); // Stops every single active emission of this sound
        }
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
