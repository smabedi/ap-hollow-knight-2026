package com.smabedi.hollowknight.config;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class AudioManager {
    private static AudioManager instance;
    private Music activeMusic;
    private Music pendingMusic;
    private float fadeTimer = 0f;
    private static final float FADE_DURATION = 0.8f; // 0.8s fade out + 0.8s fade in
    private enum FadeState { NONE, FADING_OUT, FADING_IN }
    private FadeState fadeState = FadeState.NONE;
    private static final Random random = new Random();
    private final Map<Long, SoundInstance> activeLoops = new HashMap<>();

    // Internal class to track which sound object owns which ID
    private static class SoundInstance {
        Sound sound;
        String path;

        SoundInstance(Sound sound, String path) {
            this.sound = sound;
            this.path = path;
        }
    }

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Updates music transitions. Hooked directly into the main GameScreen loop.
     */
    public void update(float dt) {
        float maxMusicVolume = GameSettings.getMusicVolume();
        boolean shouldPlayMusic = GameSettings.shouldPlayMusic();

        if (!shouldPlayMusic) {
            if (activeMusic != null && activeMusic.isPlaying()) activeMusic.pause();
            if (pendingMusic != null && pendingMusic.isPlaying()) pendingMusic.pause();
            return;
        }

        switch (fadeState) {
            case FADING_OUT -> {
                if (activeMusic != null && activeMusic.isPlaying()) {
                    fadeTimer += dt;
                    float progress = MathUtils.clamp(fadeTimer / FADE_DURATION, 0f, 1f);
                    activeMusic.setVolume((1f - progress) * maxMusicVolume);

                    if (progress >= 1f) {
                        activeMusic.stop(); // Cleanly kill track A before starting track B
                        activeMusic = pendingMusic;
                        pendingMusic = null;

                        if (activeMusic != null) {
                            activeMusic.setVolume(0f);
                            activeMusic.play();
                            fadeState = FadeState.FADING_IN;
                            fadeTimer = 0f;
                        } else {
                            fadeState = FadeState.NONE;
                        }
                    }
                } else {
                    // Fallback if active track was already stopped
                    activeMusic = pendingMusic;
                    pendingMusic = null;
                    if (activeMusic != null) {
                        activeMusic.setVolume(0f);
                        activeMusic.play();
                        fadeState = FadeState.FADING_IN;
                        fadeTimer = 0f;
                    } else {
                        fadeState = FadeState.NONE;
                    }
                }
            }
            case FADING_IN -> {
                if (activeMusic != null) {
                    if (!activeMusic.isPlaying()) activeMusic.play();
                    fadeTimer += dt;
                    float progress = MathUtils.clamp(fadeTimer / FADE_DURATION, 0f, 1f);
                    activeMusic.setVolume(progress * maxMusicVolume);

                    if (progress >= 1f) {
                        activeMusic.setVolume(maxMusicVolume);
                        fadeState = FadeState.NONE;
                    }
                } else {
                    fadeState = FadeState.NONE;
                }
            }
            case NONE -> {
                if (activeMusic != null && activeMusic.isPlaying()) {
                    activeMusic.setVolume(maxMusicVolume);
                }
            }
        }

        // Continuous SFX Mute Logic
        boolean shouldPlaySFX = GameSettings.shouldPlaySFX();
        for (Map.Entry<Long, SoundInstance> entry : activeLoops.entrySet()) {
            if (!shouldPlaySFX) {
                entry.getValue().sound.pause(entry.getKey());
            } else {
                entry.getValue().sound.resume(entry.getKey());
            }
        }
    }

    private void playMusicCore(String internalPath, boolean loop) {
        if (!Assets.getInstance().manager.isLoaded(internalPath, Music.class)) {
            System.err.println("Music asset not loaded yet: " + internalPath);
            return;
        }

        Music target = Assets.getInstance().manager.get(internalPath, Music.class);

        // Ignore if this track is already playing or queued
        if (activeMusic == target && (fadeState == FadeState.NONE || fadeState == FadeState.FADING_IN)) return;
        if (pendingMusic == target) return;

        target.setLooping(loop);

        // If no active music, jump straight to Fade In
        if (activeMusic == null || !activeMusic.isPlaying()) {
            activeMusic = target;
            pendingMusic = null;
            if (GameSettings.shouldPlayMusic()) {
                activeMusic.setVolume(0f);
                activeMusic.play();
                fadeState = FadeState.FADING_IN;
                fadeTimer = 0f;
            }
        } else {
            // Queue target and start Sequential Fade Out
            pendingMusic = target;
            fadeState = FadeState.FADING_OUT;
            fadeTimer = 0f;
        }
    }

    public void stopCurrentMusic() {
        if (activeMusic != null) activeMusic.stop();
        if (pendingMusic != null) pendingMusic.stop();
        activeMusic = null;
        pendingMusic = null;
        fadeState = FadeState.NONE;
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
        long id = sound.loop(1f, 1f, 0f);

        // Immediately pause if muted, but keep tracking the ID!
        if (!GameSettings.shouldPlaySFX()) sound.pause(id);

        getInstance().activeLoops.put(id, new SoundInstance(sound, internalPath));
        return id;
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
        long id = sound.loop(volume, 1f, pan);
        if (!GameSettings.shouldPlaySFX()) sound.pause(id);
        getInstance().activeLoops.put(id, new SoundInstance(sound, internalPath));
        return id;
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
        // LibGDX overrides pause states when volume is updated, so we must gate it
        if (GameSettings.shouldPlaySFX()) {
            sound.setPan(soundId, pan, volume);
        }
    }

    /**
     * Safely terminates a specific looped sound instance.
     */
    public static void stopSfx(String internalPath, long soundId) {
        if (soundId == -1) return;
        if (!Assets.getInstance().manager.isLoaded(internalPath, Sound.class)) return;

        Sound sound = Assets.getInstance().manager.get(internalPath, Sound.class);
        sound.stop(soundId);
        // Remove from the active registry
        getInstance().activeLoops.remove(soundId);
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
        // Clear the registry to prevent memory leaks
        getInstance().activeLoops.clear();
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
}
